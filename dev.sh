#!/usr/bin/env bash
# 一键编译并启动后端 + 前端 + 管理后台
# 用法:
#   ./dev.sh          前台运行（Ctrl+C 停止所有服务）
#   ./dev.sh -d       后台运行（关掉终端不退出），日志: /tmp/memorial-dev.log
#   ./dev.sh stop     停止后台运行的服务
#   ./dev.sh status   查看运行状态
# 环境变量覆盖（可选）: JAVA_HOME, MVN_BIN
# 首次启动会自动检测并安装前端依赖（npm install）

set -e

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
BACKEND_JAR="$PROJECT_ROOT/ruoyi-admin/target/ruoyi-admin.jar"
BACKEND_LOG="/tmp/memorial-backend.log"
FRONTEND_LOG="/tmp/memorial-frontend.log"
ADMIN_LOG="/tmp/memorial-admin.log"
BUILD_LOG="/tmp/memorial-build.log"
DEV_LOG="/tmp/memorial-dev.log"
PID_FILE="/tmp/memorial-dev.pid"
BACKEND_PORT=18080
FRONTEND_PORT=5173
ADMIN_PORT=8008
FRONTEND_HOST=0.0.0.0
FRONTEND_DIR="$PROJECT_ROOT/memorial-app"
ADMIN_DIR="$PROJECT_ROOT/ruoyi-ui"

BACKEND_PID=""
FRONTEND_PID=""
ADMIN_PID=""
SCRIPT_START=$SECONDS
CLEANING=false

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

# 确保前端依赖已安装（node_modules 被 .gitignore 忽略，新机器需 npm install）
ensure_npm_deps() {
    local dir=$1 name=$2 bin=$3
    if [ ! -x "$dir/node_modules/.bin/$bin" ]; then
        echo "[$(ts)]     $name 缺少依赖（未找到 node_modules/.bin/$bin）"
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
    kill_tree "$FRONTEND_PID"
    kill_tree "$ADMIN_PID"
    kill_tree "$BACKEND_PID"
    lsof -ti :$BACKEND_PORT 2>/dev/null | xargs kill -9 2>/dev/null || true
    lsof -ti :$FRONTEND_PORT 2>/dev/null | xargs kill -9 2>/dev/null || true
    lsof -ti :$ADMIN_PORT 2>/dev/null | xargs kill -9 2>/dev/null || true
    rm -f "$PID_FILE" 2>/dev/null || true
    echo "[$(ts)]     已停止 (总运行时长 $(fmt_duration $SECONDS))"
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
    echo "端口监听:"
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

# ========== 参数解析 ==========
MODE="foreground"
case "${1:-}" in
    -d|--detach|--daemon) MODE="daemon" ;;
    --detached) MODE="detached" ;;   # 内部：被 daemon 启动器重新调用
    stop)   trap - INT TERM EXIT; stop_running; exit 0 ;;
    status) trap - INT TERM EXIT; show_status; exit 0 ;;
    -h|--help)
        echo "用法:"
        echo "  ./dev.sh          前台运行（Ctrl+C 停止所有服务）"
        echo "  ./dev.sh -d       后台运行（关掉终端不退出）"
        echo "  ./dev.sh stop     停止后台运行的服务"
        echo "  ./dev.sh status   查看运行状态"
        exit 0
        ;;
    "") MODE="foreground" ;;
    *) echo "未知参数: $1（用法: ./dev.sh [-d|stop|status]）"; exit 1 ;;
esac

# daemon 启动器：后台重新调用自己为 --detached，然后直接退出（不触发 cleanup）
if [ "$MODE" = "daemon" ]; then
    trap - INT TERM EXIT
    : > "$DEV_LOG"
    if command -v setsid >/dev/null 2>&1; then
        setsid bash "$0" --detached >> "$DEV_LOG" 2>&1 < /dev/null &
    else
        nohup bash "$0" --detached >> "$DEV_LOG" 2>&1 < /dev/null &
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

# ========== [1/4] 编译后端 ==========
echo ""
echo "[$(ts)] ==> [1/4] 编译后端 (mvn package)..."
echo "[$(ts)]     命令: JAVA_HOME=$JAVA_HOME_DIR $MVN_BIN -pl ruoyi-admin -am package -DskipTests -B"
echo "[$(ts)]     提示: 首次构建需下载依赖，可能 5-15 分钟；后续构建约 30 秒"
echo "[$(ts)]     构建日志同时写入: $BUILD_LOG"
echo "[$(ts)]     --- Maven 输出开始 ---"

BUILD_START=$SECONDS
JAVA_HOME=$JAVA_HOME_DIR "$MVN_BIN" -pl ruoyi-admin -am package -DskipTests -B 2>&1 | tee "$BUILD_LOG"
MVN_EXIT=${PIPESTATUS[0]}
BUILD_END=$SECONDS

echo "[$(ts)]     --- Maven 输出结束 ---"
if [ "$MVN_EXIT" -ne 0 ]; then
    echo "[$(ts)]     ✗ 编译失败 (Maven exit $MVN_EXIT，用时 $(fmt_duration $((BUILD_END-BUILD_START))))"
    echo "             查看完整日志: $BUILD_LOG"
    exit 1
fi
if [ ! -f "$BACKEND_JAR" ]; then
    echo "[$(ts)]     ✗ 编译异常：$BACKEND_JAR 未生成"
    exit 1
fi
echo "[$(ts)]     ✓ 编译完成 (用时 $(fmt_duration $((BUILD_END-BUILD_START))))"

# ========== [2/4] 启动后端 ==========
echo ""
echo "[$(ts)] ==> [2/4] 启动后端..."
if lsof -ti :$BACKEND_PORT >/dev/null 2>&1; then
    echo "[$(ts)]     端口 $BACKEND_PORT 被占用，先停止旧进程"
    lsof -ti :$BACKEND_PORT | xargs kill -9 2>/dev/null || true
    sleep 1
fi

: > "$BACKEND_LOG"
nohup "$JAVA_HOME_DIR/bin/java" -jar "$BACKEND_JAR" > "$BACKEND_LOG" 2>&1 &
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
    # 进度提示：每秒一个点，每 10 秒换行并打印日志最后一行
    if [ $((i % 10)) -eq 0 ]; then
        local_last=$(tail -1 "$BACKEND_LOG" 2>/dev/null)
        echo ""
        echo "[$(ts)]     [${i}s] $local_last"
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

# ========== [3/4] 启动前端 ==========
echo ""
echo "[$(ts)] ==> [3/4] 启动前端 (memorial-app H5)..."
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
        local_last=$(tail -1 "$FRONTEND_LOG" 2>/dev/null)
        echo ""
        echo "[$(ts)]     [${i}s] $local_last"
    else
        printf "."
    fi
    sleep 1
done
if [ "$FE_READY" != "true" ]; then
    echo ""
    echo "[$(ts)]     ! 前端 30s 内未检测到就绪标志，继续 tail 日志（可能仍在启动）"
fi

# ========== [4/4] 启动管理后台 ==========
echo ""
echo "[$(ts)] ==> [4/4] 启动管理后台 (ruoyi-ui)..."
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
        local_last=$(tail -1 "$ADMIN_LOG" 2>/dev/null)
        echo ""
        echo "[$(ts)]     [${i}s] $local_last"
    else
        printf "."
    fi
    sleep 1
done
if [ "$ADMIN_READY" != "true" ]; then
    echo ""
    echo "[$(ts)]     ! 管理后台 60s 内未检测到就绪标志，继续 tail 日志（可能仍在启动）"
fi

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
echo "  构建日志:    $BUILD_LOG"
echo "  Ctrl+C 停止所有服务"
echo "=========================================="
echo ""

if [ "$MODE" = "detached" ]; then
    # 后台守护模式：阻塞等待 ./dev.sh stop 发来的 SIGTERM，由 trap cleanup 收尾
    echo "[$(ts)] ==> 后台运行中，等待 ./dev.sh stop"
    while true; do sleep 3600 || true; done
else
    # 前台模式：tail 前端日志，Ctrl+C 触发 cleanup
    tail -f "$FRONTEND_LOG"
fi
