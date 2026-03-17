#!/bin/bash
# Talaria 로컬 서비스 기동 스크립트
# 순서: Eureka → (Gateway · Invest · Notify 병렬)

set -e

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOGS_DIR="${ROOT_DIR}/.run/logs"
PIDS_DIR="${ROOT_DIR}/.run/pids"

mkdir -p "$LOGS_DIR" "$PIDS_DIR"
cd "$ROOT_DIR"

# ── 1. Eureka ──────────────────────────────
echo "▶ [1/4] Eureka 시작 (port 8761)..."
./gradlew :talaria-eureka:bootRun --no-daemon \
    > "$LOGS_DIR/eureka.log" 2>&1 &
echo $! > "$PIDS_DIR/eureka.pid"

echo "⏳ Eureka 기동 대기 중..."
for i in $(seq 1 24); do
    if curl -sf http://localhost:8761/actuator/health > /dev/null 2>&1; then
        echo "✅ Eureka 준비 완료 (${i}회 시도)"
        break
    fi
    if (( i == 24 )); then
        echo "❌ Eureka 기동 실패 — logs: $LOGS_DIR/eureka.log"
        exit 1
    fi
    sleep 5
done

# ── 2. Gateway · Invest · Notify (병렬) ───
echo "▶ [2/4] Gateway 시작 (port 8080)..."
./gradlew :talaria-gateway:bootRun --no-daemon \
    > "$LOGS_DIR/gateway.log" 2>&1 &
echo $! > "$PIDS_DIR/gateway.pid"

echo "▶ [3/4] Invest 시작 (port 8081)..."
./gradlew :talaria-invest:bootRun --no-daemon \
    > "$LOGS_DIR/invest.log" 2>&1 &
echo $! > "$PIDS_DIR/invest.pid"

echo "▶ [4/4] Notify 시작 (port 8082)..."
./gradlew :talaria-notify:bootRun --no-daemon \
    > "$LOGS_DIR/notify.log" 2>&1 &
echo $! > "$PIDS_DIR/notify.pid"

echo ""
echo "🚀 Talaria 로컬 환경 기동 완료"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Eureka Dashboard : http://localhost:8761"
echo "  API Gateway      : http://localhost:8080"
echo "  Invest (직접)    : http://localhost:8081/actuator/health"
echo "  Notify (직접)    : http://localhost:8082/actuator/health"
echo "  Kafka UI         : http://localhost:8090"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "로그 확인: make logs  |  중지: make stop"
