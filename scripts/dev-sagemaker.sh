#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
FRONTEND_DIR="$PROJECT_DIR/packages/frontend"
BACKEND_DIR="$PROJECT_DIR/packages/backend"
PIDFILE="$PROJECT_DIR/.sagemaker.pids"

export SAGEMAKER=1
export NEXT_PUBLIC_BASE_PATH="/codeeditor/default/absports/3000"

kill_port() {
  local port=$1
  local pids
  pids=$(grep -E ":$(printf '%04X' "$port") " /proc/net/tcp /proc/net/tcp6 2>/dev/null \
    | awk '{print $10}' | sort -u)
  for inode in $pids; do
    for pid in $(ls /proc 2>/dev/null | grep -E '^[0-9]+$'); do
      if ls -la /proc/$pid/fd 2>/dev/null | grep -q "socket:\[$inode\]"; then
        kill -9 "$pid" 2>/dev/null || true
      fi
    done
  done
}

stop_existing() {
  if [ -f "$PIDFILE" ]; then
    echo "=== Stopping previous session ==="
    while IFS= read -r pid; do
      kill "$pid" 2>/dev/null && echo "Stopped PID $pid" || true
    done < "$PIDFILE"
    rm -f "$PIDFILE"
  fi
  for port in 3000 3001 8080; do
    kill_port "$port"
  done
  sleep 1
}

cleanup() {
  echo ""
  echo "=== Stopping all services ==="
  if [ -f "$PIDFILE" ]; then
    while IFS= read -r pid; do
      kill "$pid" 2>/dev/null || true
    done < "$PIDFILE"
    rm -f "$PIDFILE"
  fi
  for port in 3000 3001 8080; do
    kill_port "$port"
  done
  echo "Done."
}

do_start() {
  stop_existing

  echo ""
  echo "=== Building frontend ==="
  cd "$FRONTEND_DIR"
  npx next build

  echo ""
  echo "=== Starting backend (workshop profile) ==="
  cd "$BACKEND_DIR"
  ./gradlew bootRun --args='--spring.profiles.active=workshop' &
  BACKEND_PID=$!
  echo "$BACKEND_PID" > "$PIDFILE"
  echo "Backend PID: $BACKEND_PID"

  echo ""
  echo "=== Starting frontend (Next.js on :3001) ==="
  cd "$FRONTEND_DIR"
  npx next start -p 3001 &
  FRONTEND_PID=$!
  echo "$FRONTEND_PID" >> "$PIDFILE"
  echo "Frontend PID: $FRONTEND_PID"

  echo ""
  echo "=== Starting SageMaker proxy (:3000 → :3001) ==="
  node "$FRONTEND_DIR/scripts/sagemaker-proxy.mjs" &
  PROXY_PID=$!
  echo "$PROXY_PID" >> "$PIDFILE"
  echo "Proxy PID: $PROXY_PID"

  trap cleanup EXIT INT TERM

  echo ""
  echo "=========================================="
  echo "  All services running!"
  echo "  Access: https://<studio-domain>/codeeditor/default/absports/3000/"
  echo "  Press Ctrl+C to stop all services"
  echo "=========================================="
  echo ""

  wait
}

CMD="${1:-start}"

case "$CMD" in
  start)
    do_start
    ;;
  stop)
    stop_existing
    echo "All services stopped."
    ;;
  -h|--help|help)
    echo "Usage: $0 [start|stop]"
    echo "  start  Build frontend & start all services (default)"
    echo "  stop   Stop all running services"
    ;;
  *)
    echo "Unknown command: $CMD"
    exit 1
    ;;
esac
