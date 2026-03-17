# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Talaria** — 헤르메스의 황금 샌들. 배당주 분석 결과를 SMS·카카오톡·텔레그램·슬랙으로 전달하는 개인용 다채널 알림 플랫폼.

- 패키지명: `io.github.snuggle.talaria`
- Java 25 + Spring Boot 4.x + Spring Cloud
- Gradle Multi-project (Kotlin DSL)

## Build Commands

```bash
# 전체 빌드
./gradlew build

# 특정 모듈 빌드
./gradlew :talaria-invest:build
./gradlew :talaria-notify:build

# 실행
./gradlew :talaria-eureka:bootRun
./gradlew :talaria-gateway:bootRun
./gradlew :talaria-invest:bootRun
./gradlew :talaria-notify:bootRun

# 테스트
./gradlew test
./gradlew :talaria-invest:test

# 의존성 확인
./gradlew dependencies
```

## Module Structure

```
talaria-common   → Spring 없는 순수 Java 라이브러리 (DTO, Event, Exception)
talaria-eureka   → 서비스 디스커버리 (포트 8761)
talaria-gateway  → API 진입점, JWT/API Key 인증, Rate Limit (포트 8080)
talaria-invest   → 배당주 수집·AI 분석·추천 (포트 8081)
talaria-notify   → 다채널 알림 발송 (포트 8082)
```

## Architecture

### Kafka 토픽 (3개만 — 셀프 토픽 없음)
| 토픽 | Producer → Consumer |
|------|---------------------|
| `talaria.stock.analyzed` | invest → notify |
| `talaria.notification.result` | notify → OpenSearch Sink |
| `talaria.notification.dlq` | notify → Retry Consumer |

### Hexagonal Architecture (invest, notify)
```
adapter/in/web          → HTTP Controller
adapter/in/messaging    → Kafka Consumer  (notify only)
application/port/in/    → UseCase / Query 인터페이스
application/port/out/   → 외부 의존성 인터페이스
application/usecase/    → 비즈니스 로직 구현체
adapter/out/persistence → JPA 구현체
adapter/out/channel/    → ChannelAdapter 구현체 (notify only)
adapter/out/ai/         → Spring AI 구현체 (invest only)
adapter/out/messaging/  → Kafka Producer 구현체
```

### ChannelAdapter 패턴
`ChannelAdapter` 인터페이스를 구현하고 `@ConditionalOnProperty`로 활성화. 새 채널 추가 = 구현체 클래스 하나만 작성.

### LLM 장애 정책
`AnalyzeStockService.analyzeAllStocks()` — 종목별 try-catch, 실패 종목 skip 후 `schedule_log` 기록. 나머지 정상 종목은 계속 처리.

## Start Order (Docker Compose)
1. MySQL · Redis · Kafka · OpenSearch
2. talaria-eureka
3. talaria-gateway · talaria-invest · talaria-notify

## Key Docs
- `docs/00-project-overview.md` — 전체 플로우 Mermaid
- `docs/01-stack-decision.md` — 각 스택 선택 이유
- `docs/02-services.md` — 서비스별 상세
- `docs/03-db-schema.md` — DB 스키마 (invest + notify)
- `docs/04-api-spec.md` — API 명세
- `docs/05-impl/talaria-invest.md` — invest 구현 가이드 (WHY 중심, 10단계 체크리스트)
- `docs/05-impl/talaria-notify.md` — notify 구현 가이드 (WHY 중심, 9단계 체크리스트)
