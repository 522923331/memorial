#!/usr/bin/env bash
# 一键编译并启动后端 + 前端
# 用法: ./dev.sh
# Ctrl+C 停止所有服务

set -e

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
JAVA_HOME_DIR="/Users/k02/Library/Java/JavaVirtualMachines/ms-17.0.15/Contents/Home"
MVN_BIN="/opt/homebrew/bin/mvn"
BACKEND_JAR="$PROJECT_ROOT/ruoyi-admin/target/ruoyi-admin.jar"
BACKEND_LOG="/tmp/memorial-backend.log"
FRONTEND_LOG="/tmp/memorial-frontend.log"
BACKEND_PORT=18080
FRONTEND_DIR="$PROJECT_ROOT/memorial-app"

BACKEND_PID=""
FRONTEND_PID=""

# 递归杀掉进程及其所有子进程（用于清理 npm → vite 的进程树）
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
    echo ""
    echo "==> 停止所有服务..."
    kill_tree "$FRONTEND_PID"
    kill_tree "$BACKEND_PID"
    lsof -ti :$BACKEND_PORT 2>/dev/null | xargs kill -9 2>/dev/null || true
    echo "    已停止"
    exit 0
}
trap cleanup INT TERM

# === [1/3] 编译后端 ===
echo "==> [1/3] 编译后端 (mvn package)..."
cd "$PROJECT_ROOT"
JAVA_HOME=$JAVA_HOME_DIR "$MVN_BIN" -pl ruoyi-admin -am package -DskipTests -q
if [ ! -f "$BACKEND_JAR" ]; then
    echo "    ✗ 编译失败：$BACKEND_JAR 未生成"
    exit 1
fi
echo "    ✓ 编译完成"

# === [2/3] 启动后端 ===
echo "==> [2/3] 启动后端..."
if lsof -ti :$BACKEND_PORT >/dev/null 2>&1; then
    echo "    端口 $BACKEND_PORT 被占用，先停止旧进程"
    lsof -ti :$BACKEND_PORT | xargs kill -9 2>/dev/null || true
    sleep 1
fi

nohup java -jar "$BACKEND_JAR" > "$BACKEND_LOG" 2>&1 &
BACKEND_PID=$!
echo "    后端 PID=$BACKEND_PID, 日志=$BACKEND_LOG"

echo "    等待后端就绪..."
READY=false
for i in $(seq 1 60); do
    if grep -q "Started RuoYiApplication" "$BACKEND_LOG" 2>/dev/null; then
        echo "    ✓ 后端就绪 (${i}s)"
        READY=true
        break
    fi
    if grep -qE "APPLICATION FAILED TO START|Error starting ApplicationContext" "$BACKEND_LOG" 2>/dev/null; then
        echo "    ✗ 后端启动失败，最后 20 行日志:"
        tail -20 "$BACKEND_LOG"
        exit 1
    fi
    sleep 1
done
if [ "$READY" != "true" ]; then
    echo "    ✗ 后端 60s 内未就绪，查看日志: $BACKEND_LOG"
    exit 1
fi

# === [3/3] 启动前端 ===
echo "==> [3/3] 启动前端 (memorial-app H5)..."
cd "$FRONTEND_DIR"
nohup npm run dev:h5 > "$FRONTEND_LOG" 2>&1 &
FRONTEND_PID=$!
echo "    前端 PID=$FRONTEND_PID, 日志=$FRONTEND_LOG"

echo ""
echo "=========================================="
echo "  启动完成"
echo "  后端 API:  http://localhost:$BACKEND_PORT"
echo "  前端 H5:   见下方日志 (默认 http://localhost:5173)"
echo "  后端日志:  tail -f $BACKEND_LOG"
echo "  前端日志:  tail -f $FRONTEND_LOG"
echo "  Ctrl+C 停止所有服务"
echo "=========================================="
echo ""

# 前台 tail 前端日志，Ctrl+C 会触发 cleanup
tail -f "$FRONTEND_LOG"
