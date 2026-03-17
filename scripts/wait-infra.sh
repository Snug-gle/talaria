#!/bin/bash
# 인프라 컨테이너 헬스체크 대기 스크립트

REQUIRED=("talaria-mysql" "talaria-redis" "talaria-kafka")
MAX_WAIT=120
INTERVAL=5

elapsed=0
while true; do
    all_healthy=true
    for name in "${REQUIRED[@]}"; do
        status=$(docker inspect --format='{{.State.Health.Status}}' "$name" 2>/dev/null)
        if [[ "$status" != "healthy" ]]; then
            all_healthy=false
            break
        fi
    done

    if $all_healthy; then
        echo "✅ 인프라 준비 완료 (${elapsed}s)"
        exit 0
    fi

    if (( elapsed >= MAX_WAIT )); then
        echo "❌ 인프라 기동 타임아웃 (${MAX_WAIT}s)"
        docker compose ps
        exit 1
    fi

    printf "  [%ds] MySQL/Redis/Kafka 기동 중...\r" "$elapsed"
    sleep "$INTERVAL"
    (( elapsed += INTERVAL ))
done
