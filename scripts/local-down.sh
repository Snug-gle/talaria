#!/bin/bash
# Talaria Spring Boot 서비스 중지 스크립트

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PIDS_DIR="${ROOT_DIR}/.run/pids"

echo "⏹ Talaria 서비스 중지 중..."

# PID 파일 기반 종료
if [ -d "$PIDS_DIR" ]; then
    for pid_file in "$PIDS_DIR"/*.pid; do
        [ -f "$pid_file" ] || continue
        svc=$(basename "$pid_file" .pid)
        pid=$(cat "$pid_file")
        if kill -0 "$pid" 2>/dev/null; then
            kill "$pid" 2>/dev/null
            echo "  ✓ $svc (PID $pid) 중지"
        fi
        rm -f "$pid_file"
    done
fi

# 포트 기반 Fallback (PID 파일 없을 때)
for port in 8761 8080 8081 8082; do
    pid=$(lsof -t -i:"$port" 2>/dev/null || true)
    if [ -n "$pid" ]; then
        kill "$pid" 2>/dev/null || true
        echo "  ✓ 포트 $port 프로세스 종료"
    fi
done

echo "✅ 서비스 중지 완료"
