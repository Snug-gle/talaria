.PHONY: help dev stop infra down build logs logs-eureka logs-gateway logs-invest logs-notify clean

CYAN  := \033[36m
RESET := \033[0m

help: ## 사용 가능한 명령어 목록
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) \
		| awk 'BEGIN {FS = ":.*?## "}; {printf "$(CYAN)%-18s$(RESET) %s\n", $$1, $$2}'

# ──────────────────────────────────────────
# 전체 로컬 환경
# ──────────────────────────────────────────

dev: infra build ## 인프라 + 전체 서비스 시작
	@bash scripts/local-up.sh

stop: ## Spring Boot 서비스만 중지
	@bash scripts/local-down.sh

# ──────────────────────────────────────────
# 인프라 (Docker Compose)
# ──────────────────────────────────────────

infra: ## Docker 인프라 시작 (MySQL·Redis·Kafka·OpenSearch)
	docker compose up -d
	@echo "⏳ 인프라 헬스체크 대기 중..."
	@bash scripts/wait-infra.sh

down: stop ## 서비스 중지 + Docker 인프라 중지
	docker compose down

# ──────────────────────────────────────────
# 빌드
# ──────────────────────────────────────────

build: ## 전체 빌드 (테스트 제외)
	./gradlew build -x test

# ──────────────────────────────────────────
# 로그
# ──────────────────────────────────────────

logs: ## 전체 서비스 로그 (tail -f)
	@tail -f .run/logs/*.log

logs-eureka: ## Eureka 로그
	@tail -f .run/logs/eureka.log

logs-gateway: ## Gateway 로그
	@tail -f .run/logs/gateway.log

logs-invest: ## Invest 로그
	@tail -f .run/logs/invest.log

logs-notify: ## Notify 로그
	@tail -f .run/logs/notify.log

# ──────────────────────────────────────────
# 정리
# ──────────────────────────────────────────

clean: down ## 서비스·인프라 중지 + 빌드 산출물 정리
	./gradlew clean
	@rm -rf .run
