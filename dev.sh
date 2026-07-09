#!/usr/bin/env bash
# 一键编译并启动后端 + 前端 + 管理后台
#
# 运行模式自动按系统判断（可用 --prod / --dev 覆盖）：
#   Linux            -> 生产模式：build 前端产物 + 部署到 nginx 目录 + 启动后端 jar（不启 dev server）
#   macOS / Windows  -> 开发模式：启动 3 个 dev server（后端 jar + H5 5173 + 管理后台 8008），热更新调试
#
# 用法:
#   ./dev.sh              按系统自动选模式，前台运行（Ctrl+C 停止）
#   ./dev.sh -d           后台运行（关掉终端不退出），日志: /tmp/memorial-dev.log
#   ./dev.sh -c           强制 clean 重新编译后端（可与 -d / --prod / --dev 组合）
#   ./dev.sh --prod       强制生产模式（build + 部署 + 后端 jar）
#   ./dev.sh --dev        强制开发模式（3 个 dev server）
#   ./dev.sh stop         停止后台运行的服务
#   ./dev.sh status       查看运行状态
#
# 编译过程实时输出：Maven/npm 是 Java/Node 进程，stdout 接管道后变块缓冲，
# 长时间不刷新易被误认为卡住。本脚本用 PTY（unbuffer / script）强制行缓冲，
# 并辅以 30s 心跳回显日志末行，双保险让编译过程始终可见。
#
# 环境变量覆盖（可选）: JAVA_HOME, MVN_BIN, NGINX_HTML_ROOT, SERVER_PUBLIC_IP
# 首次启动会自动检测并安装前端依赖（npm install）

set -e

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
BACKEND_JAR="$PROJECT_ROOT/ruoyi-admin/target/ruoyi-admin.jar"
BACKEND_LOG="/tmp/memorial-backend.log"
FRONTEND_LOG="/tmp/memorial-frontend.log"
ADMIN_LOG="/tmp/memorial-admin.log"
BACKEND_BUILD_LOG="/tmp/memorial-build-backend.log"
H5_BUILD_LOG="/tmp/memorial-build-h5.log"
ADMIN_BUILD_LOG="/tmp/memorial-build-admin.log"
DEV_LOG="/tmp/memorial-dev.log"
PID_FILE="/tmp/memorial-dev.pid"
BACKEND_PORT=18080
FRONTEND_PORT=5173
ADMIN_PORT=8008
FRONTEND_HOST=0.0.0.0
FRONTEND_DIR="$PROJECT_ROOT/memorial-app"
ADMIN_DIR="$PROJECT_ROOT/ruoyi-ui"
NGINX_HTML_ROOT="${NGINX_HTML_ROOT:-/usr/share/nginx/html}"
H5_BUILD_DIR="$FRONTEND_DIR/dist/build/h5"
ADMIN_BUILD_DIR="$ADMIN_DIR/dist"

BACKEND_PID=""
FRONTEND_PID=""
ADMIN_PID=""
HEARTBEAT_PID=""
SCRIPT_START=$SECONDS
CLEANING=false
RUN_MODE_OVERRIDE=""

# ========== 工具函数 ==========

# 时间戳前缀
ts() { date +"%H:%M:%S"; }

# 格式化耗时
fmt_duration() {
    local s=$1
    printf "%dm%02ds" $((s/60)) $((s%60))
}

# 解析符号链接到真实路径（macOS 和 Linux 通用）
real_path() {
    realpath "$1" 2>/dev/null || readlink -f "$1" 2>/dev/null || echo "$1"
}

# 获取本机网卡 IP（用于打印访问地址；取不到则回退 localhost）
# 注：云服务器上通常返回内网 IP，外网访问需替换为服务器公网 IP
get_local_ip() {
    local ip
    ip=$(hostname -I 2>/dev/null | awk '{print $1}')
    [ -n "$ip" ] && { echo "$ip"; return; }
    ip=$(ipconfig getifaddr en0 2>/dev/null || true)
    [ -n "$ip" ] && { echo "$ip"; return; }
    echo "localhost"
}

# 用 PTY 运行命令，让子进程 stdout 行缓冲（解决 Java/Maven 经管道块缓冲导致输出不及时）
# 优先 unbuffer（expect 自带），其次 script（Linux/macOS 语法不同），都没有则退化直接运行
run_with_pty() {
    if command -v unbuffer >/dev/null 2>&1; then
        unbuffer "$@"
        return $?
    fi
    if command -v script >/dev/null 2>&1; then
        case "$(uname -s)" in
            Darwin) script -q /dev/null "$@"; return $? ;;
            *)      script -qec "$*" /dev/null; return $? ;;
        esac
    fi
    "$@"
}

# 心跳：每 N 秒回显日志末行 + 字节数，证明编译进程在动（防止长时间无输出被误认为卡住）
# 用法: heartbeat <log_file> <label> [interval_sec]
heartbeat() {
    local log=$1 label=$2 interval=${3:-30}
    local n=0
    while true; do
        sleep "$interval"
        n=$((n + interval))
        if [ -f "$log" ]; then
            local sz last
            sz=$(wc -c < "$log" 2>/dev/null || echo 0)
            last=$(tail -1 "$log" 2>/dev/null | cut -c1-100)
            echo "[$(ts)]     ⏳ ${label}进行中 (${n}s, 日志 ${sz} 字节): ${last}"
        else
            echo "[$(ts)]     ⏳ ${label}进行中 (${n}s)..."
        fi
    done
}

# 启动心跳后台进程（HEARTBEAT_PID 全局，cleanup 会一并杀掉）
start_heartbeat() {
    heartbeat "$1" "$2" "${3:-30}" &
    HEARTBEAT_PID=$!
}

stop_heartbeat() {
    [ -n "$HEARTBEAT_PID" ] && kill "$HEARTBEAT_PID" 2>/dev/null || true
    HEARTBEAT_PID=""
}

# 确保前端依赖已安装（node_modules 被 .gitignore 忽略，新机器需 npm install）
ensure_npm_deps() {
    local dir=$1 name=$2 bin=$3
    if [ ! -x "$dir/node_modules/.bin/$bin" ]; then
        echo "[$(ts)]     $name 缺少依赖（未找到 node_modules/.bin/${bin}）"
        echo "[$(ts)]     ==> 执行 npm install（首次较慢，可能数分钟）..."
        (cd "$dir" && npm install --no-audit --no-fund) || {
            echo "[$(ts)]     ✗ $name npm install 失败"
            exit 1
        }
        echo "[$(ts)]     ✓ $name 依赖安装完成"
    fi
}

# 检查 Java 版本 >= 17（pom.xml 要求 java.version=17）
check_java_version() {
    local jh=$1
    local v major
    v=$("$jh/bin/java" -version 2>&1 | head -1)
    if echo "$v" | grep -q 'version "1\.'; then
        major=$(echo "$v" | sed -n 's/.*version "1\.\([0-9]*\).*/\1/p')
    else
        major=$(echo "$v" | sed -n 's/.*version "\([0-9]*\).*/\1/p')
    fi
    [ -n "$major" ] && [ "$major" -ge 17 ]
}

# 检测 JAVA_HOME（macOS + Linux 通用）
detect_java_home() {
    if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        if check_java_version "$JAVA_HOME"; then
            echo "$JAVA_HOME"
            return 0
        else
            echo "    ! JAVA_HOME=$JAVA_HOME 版本低于 17，尝试其他路径" >&2
        fi
    fi
    if [ -x /usr/libexec/java_home ]; then
        local jh
        jh=$(/usr/libexec/java_home -v 17 2>/dev/null) || jh=""
        if [ -n "$jh" ] && [ -x "$jh/bin/java" ]; then
            echo "$jh"
            return 0
        fi
    fi
    local candidate
    for candidate in /usr/lib/jvm/java-17-* /usr/lib/jvm/java-17 \
                     /usr/local/java/jdk-17* /opt/java/jdk-17* \
                     /usr/lib/jvm/temurin-17-* /usr/lib/jvm/zulu-17-*; do
        if [ -x "$candidate/bin/java" ] && check_java_version "$candidate"; then
            echo "$candidate"
            return 0
        fi
    done
    local java_bin real_java jh
    java_bin=$(command -v java 2>/dev/null)
    if [ -n "$java_bin" ]; then
        real_java=$(real_path "$java_bin")
        jh="${real_java%/bin/java}"
        if [ "$jh" != "$real_java" ] && [ -x "$jh/bin/java" ] && check_java_version "$jh"; then
            echo "$jh"
            return 0
        fi
    fi
    return 1
}

# 检查 Maven 版本 >= 3.6.3
check_mvn_version() {
    local mvn=$1 ver major minor patch
    ver=$("$mvn" --version 2>/dev/null | head -1 | grep -oE '[0-9]+\.[0-9]+\.[0-9]+' | head -1)
    [ -z "$ver" ] && return 1
    major=$(echo "$ver" | cut -d. -f1)
    minor=$(echo "$ver" | cut -d. -f2)
    patch=$(echo "$ver" | cut -d. -f3)
    major=${major:-0}; minor=${minor:-0}; patch=${patch:-0}
    if [ "$major" -gt 3 ]; then return 0; fi
    if [ "$major" -eq 3 ] && [ "$minor" -gt 6 ]; then return 0; fi
    if [ "$major" -eq 3 ] && [ "$minor" -eq 6 ] && [ "$patch" -ge 3 ]; then return 0; fi
    return 1
}

# 检测 Maven 二进制路径
detect_mvn() {
    if [ -n "$MVN_BIN" ] && [ -x "$MVN_BIN" ] && check_mvn_version "$MVN_BIN"; then
        echo "$MVN_BIN"
        return 0
    fi
    local mvn_in_path
    mvn_in_path=$(command -v mvn 2>/dev/null)
    if [ -n "$mvn_in_path" ] && check_mvn_version "$mvn_in_path"; then
        echo "$mvn_in_path"
        return 0
    fi
    local candidate
    for candidate in /opt/homebrew/bin/mvn /usr/local/bin/mvn \
                     /opt/maven/bin/mvn /usr/share/maven/bin/mvn; do
        if [ -x "$candidate" ] && check_mvn_version "$candidate"; then
            echo "$candidate"
            return 0
        fi
    done
    return 1
}

# 检测运行模式：Linux -> prod，其它（macOS/Windows）-> dev；允许 RUN_MODE_OVERRIDE 覆盖
detect_run_mode() {
    if [ -n "$RUN_MODE_OVERRIDE" ]; then
        echo "$RUN_MODE_OVERRIDE"
        return
    fi
    case "$(uname -s)" in
        Linux) echo "prod" ;;
        *) echo "dev" ;;   # Darwin (macOS) / MINGW* / MSYS* (Windows Git Bash) 等
    esac
}

# 递归杀进程树
kill_tree() {
    local pid=$1
    [ -z "$pid" ] && return
    local child
    for child in $(pgrep -P "$pid" 2>/dev/null); do
        kill_tree "$child"
    done
    kill "$pid" 2>/dev/null || true
}

cleanup() {
    [ "$CLEANING" = "true" ] && return
    CLEANING=true
    echo ""
    echo "[$(ts)] ==> 停止所有服务..."
    stop_heartbeat
    kill_tree "$FRONTEND_PID"
    kill_tree "$ADMIN_PID"
    kill_tree "$BACKEND_PID"
    lsof -ti :$BACKEND_PORT 2>/dev/null | xargs kill -9 2>/dev/null || true
    lsof -ti :$FRONTEND_PORT 2>/dev/null | xargs kill -9 2>/dev/null || true
    lsof -ti :$ADMIN_PORT 2>/dev/null | xargs kill -9 2>/dev/null || true
    rm -f "$PID_FILE" 2>/dev/null || true
    echo "[$(ts)]     已停止 (总运行时长 $(fmt_duration $SECONDS))"
    echo "[$(ts)]     （生产模式下前端为 nginx 静态文件，不会被动；如需更新重新执行 ./dev.sh）"
    exit 0
}

# 停止后台运行的服务（./dev.sh stop）
stop_running() {
    local pid
    if [ -f "$PID_FILE" ]; then
        pid=$(cat "$PID_FILE" 2>/dev/null || true)
        if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
            echo "[$(ts)] ==> 停止后台服务 (PID=$pid)..."
            kill -TERM "$pid" 2>/dev/null || true
            for i in $(seq 1 20); do
                kill -0 "$pid" 2>/dev/null || break
                sleep 0.5
            done
            if kill -0 "$pid" 2>/dev/null; then
                echo "[$(ts)]     未退出，强制 kill -9"
                kill -9 "$pid" 2>/dev/null || true
            fi
            rm -f "$PID_FILE"
            echo "[$(ts)]     ✓ 已停止"
            return 0
        fi
        rm -f "$PID_FILE"
    fi
    echo "[$(ts)]     未找到运行记录，按端口清理残留进程..."
    local killed=false p
    for p in $BACKEND_PORT $FRONTEND_PORT $ADMIN_PORT; do
        if lsof -ti :$p >/dev/null 2>&1; then
            lsof -ti :$p | xargs kill -9 2>/dev/null || true
            killed=true
        fi
    done
    [ "$killed" = "true" ] && echo "[$(ts)]     ✓ 已清理端口残留" || echo "[$(ts)]     无运行中的服务"
}

# 查看运行状态（./dev.sh status）
show_status() {
    local pid
    if [ -f "$PID_FILE" ]; then
        pid=$(cat "$PID_FILE" 2>/dev/null || true)
        if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
            echo "[$(ts)] 运行中 (主进程 PID=$pid)"
            echo "       日志: tail -f $DEV_LOG"
        else
            echo "[$(ts)] 未运行（PID 文件已过期，已清理）"
            rm -f "$PID_FILE"
        fi
    else
        echo "[$(ts)] 未运行（无 PID 文件）"
    fi
    echo ""
    echo "端口监听（生产模式仅后端 18080 有进程，前端由 nginx 提供）:"
    local p who
    for p in $BACKEND_PORT $FRONTEND_PORT $ADMIN_PORT; do
        who=$(lsof -ti :$p 2>/dev/null || true)
        if [ -n "$who" ]; then
            echo "  :$p  ✓ 监听中 (PID $who)"
        else
            echo "  :$p  ✗ 未监听"
        fi
    done
}

# 启动后端 jar 并等待就绪。$1=prod|dev（prod 带 JVM 参数）
start_backend() {
    local mode=$1
    echo ""
    echo "[$(ts)] ==> 启动后端..."
    if lsof -ti :$BACKEND_PORT >/dev/null 2>&1; then
        echo "[$(ts)]     端口 $BACKEND_PORT 被占用，先停止旧进程"
        lsof -ti :$BACKEND_PORT | xargs kill -9 2>/dev/null || true
        sleep 1
    fi

    : > "$BACKEND_LOG"
    local PROFILE
    if [ "$mode" = "prod" ]; then
        PROFILE="prod,druid"
    else
        PROFILE="dev,druid"
    fi
    if [ "$mode" = "prod" ]; then
        # 生产 JVM 参数（JDK17 兼容，去掉 ry.sh 里已废弃的 -XX:+PrintGC*）
        local JVM_OPTS="-Duser.timezone=Asia/Shanghai -Xms512m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError -XX:NewRatio=1 -XX:SurvivorRatio=30 -XX:+UseParallelGC -XX:+UseParallelOldGC"
        echo "[$(ts)]     命令: java $JVM_OPTS -jar $BACKEND_JAR --spring.profiles.active=$PROFILE"
        nohup "$JAVA_HOME_DIR/bin/java" $JVM_OPTS -jar "$BACKEND_JAR" --spring.profiles.active="$PROFILE" > "$BACKEND_LOG" 2>&1 &
    else
        echo "[$(ts)]     命令: java -jar $BACKEND_JAR --spring.profiles.active=$PROFILE"
        nohup "$JAVA_HOME_DIR/bin/java" -jar "$BACKEND_JAR" --spring.profiles.active="$PROFILE" > "$BACKEND_LOG" 2>&1 &
    fi
    BACKEND_PID=$!
    echo "[$(ts)]     后端 PID=$BACKEND_PID"
    echo "[$(ts)]     日志: $BACKEND_LOG"

    echo "[$(ts)]     等待后端就绪 (最多 60s)..."
    READY=false
    for i in $(seq 1 60); do
        if grep -q "Started RuoYiApplication" "$BACKEND_LOG" 2>/dev/null; then
            echo ""
            echo "[$(ts)]     ✓ 后端就绪 (用时 ${i}s)"
            READY=true
            break
        fi
        if grep -qE "APPLICATION FAILED TO START|Error starting ApplicationContext" "$BACKEND_LOG" 2>/dev/null; then
            echo ""
            echo "[$(ts)]     ✗ 后端启动失败，最后 20 行日志:"
            tail -20 "$BACKEND_LOG"
            exit 1
        fi
        if [ $((i % 10)) -eq 0 ]; then
            last_line=$(tail -1 "$BACKEND_LOG" 2>/dev/null)
            echo ""
            echo "[$(ts)]     [${i}s] $last_line"
        else
            printf "."
        fi
        sleep 1
    done
    if [ "$READY" != "true" ]; then
        echo ""
        echo "[$(ts)]     ✗ 后端 60s 内未就绪，查看日志: $BACKEND_LOG"
        exit 1
    fi
}

# 启动 H5 dev server（仅开发模式）
start_frontend_dev() {
    echo ""
    echo "[$(ts)] ==> 启动前端 (memorial-app H5 dev server)..."
    cd "$FRONTEND_DIR"
    ensure_npm_deps "$FRONTEND_DIR" "memorial-app" "uni"
    : > "$FRONTEND_LOG"
    # 显式 --host 0.0.0.0 让 H5 可被外网访问（uni 默认只绑 localhost）
    nohup npx uni --host $FRONTEND_HOST --port $FRONTEND_PORT > "$FRONTEND_LOG" 2>&1 &
    FRONTEND_PID=$!
    echo "[$(ts)]     前端 PID=$FRONTEND_PID"
    echo "[$(ts)]     日志: $FRONTEND_LOG"
    echo "[$(ts)]     等待 Vite 就绪 (最多 30s)..."

    FE_READY=false
    for i in $(seq 1 30); do
        if grep -qE "Local:|ready in|VITE.*ready" "$FRONTEND_LOG" 2>/dev/null; then
            echo ""
            echo "[$(ts)]     ✓ 前端就绪 (用时 ${i}s)"
            FE_READY=true
            break
        fi
        if grep -qE "error|Error|ELIFECYCLE|failed" "$FRONTEND_LOG" 2>/dev/null; then
            echo ""
            echo "[$(ts)]     ✗ 前端启动失败，最后 20 行日志:"
            tail -20 "$FRONTEND_LOG"
            exit 1
        fi
        if [ $((i % 5)) -eq 0 ]; then
            last_line=$(tail -1 "$FRONTEND_LOG" 2>/dev/null)
            echo ""
            echo "[$(ts)]     [${i}s] $last_line"
        else
            printf "."
        fi
        sleep 1
    done
    if [ "$FE_READY" != "true" ]; then
        echo ""
        echo "[$(ts)]     ! 前端 30s 内未检测到就绪标志，继续 tail 日志（可能仍在启动）"
    fi
}

# 启动管理后台 dev server（仅开发模式）
start_admin_dev() {
    echo ""
    echo "[$(ts)] ==> 启动管理后台 (ruoyi-ui dev server)..."
    cd "$ADMIN_DIR"
    ensure_npm_deps "$ADMIN_DIR" "ruoyi-ui" "vue-cli-service"
    if lsof -ti :$ADMIN_PORT >/dev/null 2>&1; then
        echo "[$(ts)]     端口 $ADMIN_PORT 被占用，先停止旧进程"
        lsof -ti :$ADMIN_PORT | xargs kill -9 2>/dev/null || true
        sleep 1
    fi
    : > "$ADMIN_LOG"
    # vue.config.js 已配置 devServer.host=0.0.0.0，管理后台默认即可外网访问
    nohup npm run dev > "$ADMIN_LOG" 2>&1 &
    ADMIN_PID=$!
    echo "[$(ts)]     管理后台 PID=$ADMIN_PID"
    echo "[$(ts)]     日志: $ADMIN_LOG"
    echo "[$(ts)]     等待管理后台就绪 (最多 60s)..."

    ADMIN_READY=false
    for i in $(seq 1 60); do
        if grep -qE "App running at|Compiled successfully" "$ADMIN_LOG" 2>/dev/null; then
            echo ""
            echo "[$(ts)]     ✓ 管理后台就绪 (用时 ${i}s)"
            ADMIN_READY=true
            break
        fi
        if grep -qE "Failed to compile|ELIFECYCLE" "$ADMIN_LOG" 2>/dev/null; then
            echo ""
            echo "[$(ts)]     ✗ 管理后台启动失败，最后 20 行日志:"
            tail -20 "$ADMIN_LOG"
            exit 1
        fi
        if [ $((i % 10)) -eq 0 ]; then
            last_line=$(tail -1 "$ADMIN_LOG" 2>/dev/null)
            echo ""
            echo "[$(ts)]     [${i}s] $last_line"
        else
            printf "."
        fi
        sleep 1
    done
    if [ "$ADMIN_READY" != "true" ]; then
        echo ""
        echo "[$(ts)]     ! 管理后台 60s 内未检测到就绪标志，继续 tail 日志（可能仍在启动）"
    fi
}

# 构建 H5 生产产物
build_h5() {
    echo ""
    echo "[$(ts)] ==> 构建 H5 生产产物 (npm run build:h5)..."
    cd "$FRONTEND_DIR"
    ensure_npm_deps "$FRONTEND_DIR" "memorial-app" "uni"
    local t=$SECONDS
    start_heartbeat "$H5_BUILD_LOG" "H5 构建" 20
    # tee 实时显示到终端 + 写日志；心跳兜底防止 vite 输出缓冲时看起来卡住
    npm run build:h5 2>&1 | tee "$H5_BUILD_LOG"
    local npm_exit=${PIPESTATUS[0]}
    stop_heartbeat
    if [ "$npm_exit" -ne 0 ] || [ ! -f "$H5_BUILD_DIR/index.html" ]; then
        echo "[$(ts)]     ✗ H5 构建失败，日志末尾:"
        tail -30 "$H5_BUILD_LOG"
        exit 1
    fi
    echo "[$(ts)]     ✓ H5 构建完成 (用时 $(fmt_duration $((SECONDS-t)))) -> $H5_BUILD_DIR"
}

# 构建管理后台生产产物
build_admin() {
    echo ""
    echo "[$(ts)] ==> 构建管理后台生产产物 (npm run build:prod)..."
    cd "$ADMIN_DIR"
    ensure_npm_deps "$ADMIN_DIR" "ruoyi-ui" "vue-cli-service"
    local t=$SECONDS
    start_heartbeat "$ADMIN_BUILD_LOG" "管理后台构建" 20
    npm run build:prod 2>&1 | tee "$ADMIN_BUILD_LOG"
    local npm_exit=${PIPESTATUS[0]}
    stop_heartbeat
    if [ "$npm_exit" -ne 0 ] || [ ! -f "$ADMIN_BUILD_DIR/index.html" ]; then
        echo "[$(ts)]     ✗ 管理后台构建失败，日志末尾:"
        tail -30 "$ADMIN_BUILD_LOG"
        exit 1
    fi
    echo "[$(ts)]     ✓ 管理后台构建完成 (用时 $(fmt_duration $((SECONDS-t)))) -> $ADMIN_BUILD_DIR"
}

# 部署静态产物到 nginx 目录
deploy_static() {
    echo ""
    echo "[$(ts)] ==> 部署静态文件到 nginx ($NGINX_HTML_ROOT)..."
    if [ ! -d "$NGINX_HTML_ROOT" ]; then
        echo "[$(ts)]     ! $NGINX_HTML_ROOT 不存在，nginx 可能未安装"
        echo "             请先按 docs/deployment-guide.md 安装 nginx 并配置 5.2.1"
        echo "             或用 NGINX_HTML_ROOT 环境变量指定其它目录"
    fi
    # 目录不可写时用 sudo（nginx 目录通常属 root）
    local SUDO=""
    [ -e "$NGINX_HTML_ROOT" ] && [ ! -w "$NGINX_HTML_ROOT" ] && SUDO="sudo"
    $SUDO mkdir -p "$NGINX_HTML_ROOT/h5" "$NGINX_HTML_ROOT/admin"
    $SUDO rm -rf "$NGINX_HTML_ROOT/h5" "$NGINX_HTML_ROOT/admin"
    $SUDO cp -r "$H5_BUILD_DIR" "$NGINX_HTML_ROOT/h5"
    $SUDO cp -r "$ADMIN_BUILD_DIR" "$NGINX_HTML_ROOT/admin"
    echo "[$(ts)]     ✓ H5    -> $NGINX_HTML_ROOT/h5"
    echo "[$(ts)]     ✓ admin -> $NGINX_HTML_ROOT/admin"
    # 静态文件替换无需 reload；nginx 在跑且配置 OK 时顺手 reload 保险
    if command -v nginx >/dev/null 2>&1; then
        if sudo nginx -t 2>/dev/null; then
            sudo systemctl reload nginx 2>/dev/null || sudo nginx -s reload 2>/dev/null || true
            echo "[$(ts)]     ✓ nginx 已 reload"
        else
            echo "[$(ts)]     ! nginx -t 失败，请检查 nginx 配置（见 deployment-guide.md 5.2.1）"
        fi
    fi
}

# ========== 参数解析 ==========
MODE="foreground"
DO_CLEAN=false
for arg in "$@"; do
    case "$arg" in
        -d|--detach|--daemon) MODE="daemon" ;;
        --detached) MODE="detached" ;;   # 内部：被 daemon 启动器重新调用
        -c|--clean) DO_CLEAN=true ;;     # 强制 mvn clean package 全量重编译
        --prod) RUN_MODE_OVERRIDE="prod" ;;
        --dev) RUN_MODE_OVERRIDE="dev" ;;
        stop)   trap - INT TERM EXIT; stop_running; exit 0 ;;
        status) trap - INT TERM EXIT; show_status; exit 0 ;;
        -h|--help)
            echo "用法: ./dev.sh [选项]   （默认前台运行，Ctrl+C 停止所有服务）"
            echo "运行模式自动判断：Linux -> 生产模式；macOS/Windows -> 开发模式"
            echo "选项:"
            echo "  -d, --detach    后台运行（关掉终端不退出）"
            echo "  -c, --clean     强制 clean 重新编译后端（代码改了没生效时用）"
            echo "  --prod          强制生产模式（build + 部署 nginx + 后端 jar）"
            echo "  --dev           强制开发模式（3 个 dev server）"
            echo "  stop            停止后台运行的服务"
            echo "  status          查看运行状态"
            echo "环境变量: JAVA_HOME, MVN_BIN, NGINX_HTML_ROOT(默认 /usr/share/nginx/html), SERVER_PUBLIC_IP"
            echo "组合示例:"
            echo "  ./dev.sh -d -c    后台运行 + 强制重编译"
            echo "  ./dev.sh --dev    在 Linux 上强制走开发模式调试"
            exit 0
            ;;
        *) echo "未知参数: ${arg}（用法: ./dev.sh [-d|-c|--prod|--dev|stop|status]）"; exit 1 ;;
    esac
done

# daemon 启动器：后台重新调用自己为 --detached，然后直接退出（不触发 cleanup）
if [ "$MODE" = "daemon" ]; then
    trap - INT TERM EXIT
    : > "$DEV_LOG"
    CLEAN_FLAG=""
    MODE_FLAG=""
    [ "$DO_CLEAN" = "true" ] && CLEAN_FLAG="--clean"
    [ -n "$RUN_MODE_OVERRIDE" ] && MODE_FLAG="--$RUN_MODE_OVERRIDE"
    if command -v setsid >/dev/null 2>&1; then
        setsid bash "$0" --detached $CLEAN_FLAG $MODE_FLAG >> "$DEV_LOG" 2>&1 < /dev/null &
    else
        nohup bash "$0" --detached $CLEAN_FLAG $MODE_FLAG >> "$DEV_LOG" 2>&1 < /dev/null &
        disown 2>/dev/null || true
    fi
    DAEMON_PID=$!
    echo "$DAEMON_PID" > "$PID_FILE"
    sleep 2
    if kill -0 "$DAEMON_PID" 2>/dev/null; then
        echo "[$(ts)] ✓ 已后台启动 (PID=$DAEMON_PID)"
        echo "       日志:   tail -f $DEV_LOG"
        echo "       停止:   ./dev.sh stop"
        echo "       状态:   ./dev.sh status"
    else
        echo "[$(ts)] ✗ 后台启动失败，日志末尾:"
        tail -20 "$DEV_LOG" 2>/dev/null
        rm -f "$PID_FILE"
        exit 1
    fi
    exit 0
fi
# MODE=detached 或 foreground：继续正常流程

# 仅服务模式注册 cleanup（-h/stop/status/未知参数已在前 exit，不会误杀端口占用者）
trap cleanup INT TERM EXIT

# ========== 检测环境 ==========
echo "[$(ts)] ==> 检测环境..."

JAVA_HOME_DIR=$(detect_java_home) || {
    echo "[$(ts)]     ✗ 未找到 Java >= 17"
    echo "             请安装 JDK 17+，或设置 JAVA_HOME 环境变量"
    exit 1
}
echo "[$(ts)]     JAVA_HOME = $JAVA_HOME_DIR"

MVN_BIN=$(detect_mvn) || {
    echo "[$(ts)]     ✗ 未找到 Maven >= 3.6.3"
    echo "             请安装 Maven 3.6.3+，或设置 MVN_BIN 环境变量"
    exit 1
}
echo "[$(ts)]     MVN_BIN   = $MVN_BIN"

RUN_MODE=$(detect_run_mode)
if [ "$RUN_MODE" = "prod" ]; then
    echo "[$(ts)]     运行模式   = prod（生产：build + 部署 nginx + 后端 jar，不启 dev server）"
else
    echo "[$(ts)]     运行模式   = dev（开发：后端 jar + H5 5173 + 管理后台 8008 dev server）"
fi
echo "[$(ts)]     系统       = $(uname -s)（可用 --prod / --dev 覆盖）"

# 后台模式 + 生产部署需要 sudo 时，无 TTY 输密码会卡死，提前拦截
if [ "$RUN_MODE" = "prod" ] && [ "$MODE" = "detached" ]; then
    if [ -e "$NGINX_HTML_ROOT" ] && [ ! -w "$NGINX_HTML_ROOT" ]; then
        echo ""
        echo "[$(ts)] ✗ 后台生产模式需要 sudo 部署到 $NGINX_HTML_ROOT，但后台无终端无法输密码"
        echo "           请前台执行: ./dev.sh --prod （部署完按 Ctrl+C，后端 jar 不受影响）"
        echo "           或为当前用户配置 sudo NOPASSWD（见 docs/deployment-guide.md）"
        exit 1
    fi
fi

# ========== [1] 编译后端 ==========
cd "$PROJECT_ROOT"
echo ""
if [ "$DO_CLEAN" = "true" ]; then
    MVN_GOAL="clean package"
    echo "[$(ts)] ==> 编译后端 (mvn clean package，强制全量重编译)..."
else
    MVN_GOAL="package"
    echo "[$(ts)] ==> 编译后端 (mvn package)..."
fi
echo "[$(ts)]     命令: JAVA_HOME=$JAVA_HOME_DIR $MVN_BIN -pl ruoyi-admin -am $MVN_GOAL -DskipTests -B"
echo "[$(ts)]     提示: 首次构建需下载依赖，可能 5-15 分钟；后续约 30 秒（-c 全量重编译较慢）"
echo "[$(ts)]     构建日志: $BACKEND_BUILD_LOG"
echo "[$(ts)]     --- Maven 输出开始（实时，每 30s 附心跳）---"

BUILD_START=$SECONDS
# 心跳兜底：即使 PTY 不可用导致缓冲，也每 30s 回显日志末行证明没卡住
start_heartbeat "$BACKEND_BUILD_LOG" "后端编译" 30
# run_with_pty 用 PTY 强制 Maven 行缓冲，输出经 tee 实时显示 + 写日志
run_with_pty env JAVA_HOME="$JAVA_HOME_DIR" "$MVN_BIN" -pl ruoyi-admin -am $MVN_GOAL -DskipTests -B 2>&1 | tee "$BACKEND_BUILD_LOG"
MVN_EXIT=${PIPESTATUS[0]}
BUILD_END=$SECONDS
stop_heartbeat

echo "[$(ts)]     --- Maven 输出结束 ---"
# 用 BUILD SUCCESS 字符串判断，不依赖退出码（macOS script 不透传子进程退出码）
if [ "$MVN_EXIT" -ne 0 ] || ! grep -q "BUILD SUCCESS" "$BACKEND_BUILD_LOG" 2>/dev/null; then
    echo "[$(ts)]     ✗ 编译失败 (用时 $(fmt_duration $((BUILD_END-BUILD_START))))"
    echo "             查看完整日志: $BACKEND_BUILD_LOG"
    exit 1
fi
if [ ! -f "$BACKEND_JAR" ]; then
    echo "[$(ts)]     ✗ 编译异常：$BACKEND_JAR 未生成"
    exit 1
fi
echo "[$(ts)]     ✓ 编译完成 (用时 $(fmt_duration $((BUILD_END-BUILD_START))))"

# ========== 按模式分流 ==========
if [ "$RUN_MODE" = "prod" ]; then
    # ---- 生产模式：build 前端 + 部署 + 启动后端 jar ----
    build_h5
    build_admin
    deploy_static
    start_backend prod

    echo ""
    echo "=========================================="
    echo "  生产模式启动完成 (总用时 $(fmt_duration $SECONDS))"
    PUBLIC_IP="${SERVER_PUBLIC_IP:-$(get_local_ip)}"
    echo "  H5:          http://$PUBLIC_IP/"
    echo "  管理后台:    http://$PUBLIC_IP:8080/"
    echo "  后端 API:    127.0.0.1:$BACKEND_PORT (nginx 反代 /api、/prod-api)"
    echo "  (若 $PUBLIC_IP 为内网地址，外网访问用服务器公网 IP，如 8.140.249.192)"
    echo "  nginx 配置:  docs/deployment-guide.md 5.2.1"
    echo "  后端日志:    tail -f $BACKEND_LOG"
    echo "  构建日志:    $BACKEND_BUILD_LOG / $H5_BUILD_LOG / $ADMIN_BUILD_LOG"
    echo "  Ctrl+C 停止后端（静态文件保留在 $NGINX_HTML_ROOT，不受影响）"
    echo "=========================================="
    echo ""
else
    # ---- 开发模式：3 个 dev server ----
    start_backend dev
    start_frontend_dev
    start_admin_dev

    echo ""
    echo "=========================================="
    echo "  启动完成 (总用时 $(fmt_duration $SECONDS))"
    LOCAL_IP=$(get_local_ip)
    echo "  后端 API:    http://$LOCAL_IP:$BACKEND_PORT"
    echo "  前端 H5:     http://$LOCAL_IP:$FRONTEND_PORT"
    echo "  管理后台:    http://$LOCAL_IP:$ADMIN_PORT"
    echo "  (若 $LOCAL_IP 为内网地址，外网访问请替换为服务器公网 IP)"
    echo "  后端日志:    tail -f $BACKEND_LOG"
    echo "  前端日志:    tail -f $FRONTEND_LOG"
    echo "  管理后台日志: tail -f $ADMIN_LOG"
    echo "  构建日志:    $BACKEND_BUILD_LOG"
    echo "  Ctrl+C 停止所有服务"
    echo "=========================================="
    echo ""
fi

if [ "$MODE" = "detached" ]; then
    # 后台守护模式：阻塞等待 ./dev.sh stop 发来的 SIGTERM，由 trap cleanup 收尾
    echo "[$(ts)] ==> 后台运行中，等待 ./dev.sh stop"
    while true; do sleep 3600 || true; done
else
    # 前台模式：tail 日志阻塞，Ctrl+C 触发 cleanup
    if [ "$RUN_MODE" = "prod" ]; then
        tail -f "$BACKEND_LOG"
    else
        # 开发模式同时 tail 三个日志，任一服务报错都能看到
        tail -f "$BACKEND_LOG" "$FRONTEND_LOG" "$ADMIN_LOG"
    fi
fi
