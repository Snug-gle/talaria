# Talaria — 프로젝트 개요

> 헤르메스의 날개 달린 황금 샌들 **Talaria** —
> 빠르고 경쾌하게, 배당주 소식을 다채널로 전달하는 알림 허브

---

## 프로젝트 한 줄 정의

배당주를 분석하고, 그 결과를 SMS·카카오톡·텔레그램·슬랙으로 자동 발송하는
**개인용 다채널 알림 플랫폼**

---

## 핵심 비즈니스 플로우

```mermaid
sequenceDiagram
    participant SCH as ⏰ Scheduler
    participant INV as talaria-invest
    participant EXT as 📈 한국투자증권 API
    participant AI  as 🤖 LLM (Spring AI)
    participant KAF as 📨 Kafka
    participant NOT as talaria-notify
    participant CH  as 📱 채널 (슬랙/텔레그램/카카오/SMS)
    participant OS  as 🔍 OpenSearch

    SCH->>INV: 매일 오전 8시 트리거
    INV->>EXT: 배당주 데이터 수집 요청 (50개 종목, Virtual Threads 병렬)
    EXT-->>INV: 주식 데이터 응답

    INV->>AI: 배당주 분석 요청 (프롬프트 + 데이터)
    alt LLM 정상
        AI-->>INV: 분석 결과 (추천 등급, 배당수익률 예측)
    else LLM 장애 (부분 실패 허용)
        AI-->>INV: 오류 응답
        Note over INV: 실패 종목 skip, schedule_log에 기록<br/>나머지 정상 종목은 계속 처리
    end

    INV->>INV: MySQL 저장 (성공 종목만)
    INV->>KAF: produce → talaria.stock.analyzed

    KAF->>NOT: consume ← talaria.stock.analyzed
    NOT->>CH: 채널별 병렬 발송 (Virtual Threads + Structured Concurrency)
    CH-->>NOT: 발송 결과

    NOT->>NOT: MySQL 발송 이력 저장
    NOT->>KAF: produce → talaria.notification.result
    KAF->>OS: Kafka Connect Sink → 발송 결과 저장
    Note over NOT,OS: 발송 실패 시: talaria.notification.dlq → Retry Consumer (최대 3회, 지수 백오프)
    Note over INV,NOT: 수동 발송: POST /api/notify/send → 채널 어댑터 직접 호출
```

---

## 전체 시스템 아키텍처

```mermaid
graph TB
    subgraph Client["🖥️ 클라이언트"]
        ADMIN[talaria-admin<br/>React Admin UI]
    end

    subgraph Gateway["🚪 진입점"]
        GW[talaria-gateway<br/>Spring Cloud Gateway<br/>인증 / 라우팅 / Rate Limit]
        EUR[talaria-eureka<br/>Service Discovery]
    end

    subgraph App["⚙️ 애플리케이션"]
        INV[talaria-invest<br/>배당주 수집·분석·추천]
        NOT[talaria-notify<br/>다채널 알림 발송]
    end

    subgraph External["🌐 외부 서비스"]
        STOCK_API[한국투자증권 KIS API]
        LLM[LLM API<br/>OpenAI / Claude / Ollama]
    end

    subgraph Channels["📱 알림 채널"]
        SMS[SMS Simulator]
        KAKAO[카카오톡]
        TG[텔레그램]
        SLACK[슬랙]
    end

    subgraph Streaming["📨 이벤트 스트리밍"]
        KAFKA[Apache Kafka<br/>KRaft Mode]
    end

    subgraph Storage["🗄️ 데이터 저장"]
        MYSQL[(MySQL 8.0<br/>운영 DB)]
        REDIS[(Redis 7<br/>캐시)]
        OS[(OpenSearch<br/>로그·검색·분석)]
    end

    subgraph Observability["📊 관측성"]
        PROM[Prometheus<br/>메트릭 수집]
        GRAFANA[Grafana<br/>메트릭 시각화]
        JAEGER[Jaeger<br/>분산 트레이싱]
        OSD[OpenSearch Dashboards<br/>로그 시각화]
    end

    ADMIN -->|HTTP| GW
    GW <-->|서비스 등록/조회| EUR
    GW -->|라우팅| INV
    GW -->|라우팅| NOT

    INV -->|데이터 수집| STOCK_API
    INV -->|분석 요청| LLM
    INV -->|저장| MYSQL
    INV -->|캐싱| REDIS
    INV -->|talaria.stock.analyzed| KAFKA

    KAFKA -->|consume| NOT
    NOT -->|병렬 발송| SMS
    NOT -->|병렬 발송| KAKAO
    NOT -->|병렬 발송| TG
    NOT -->|병렬 발송| SLACK
    NOT -->|저장| MYSQL
    NOT -->|talaria.notification.result<br/>talaria.notification.dlq| KAFKA

    KAFKA -->|Kafka Connect Sink| OS

    INV & NOT & GW & KAFKA -->|메트릭| PROM
    PROM --> GRAFANA
    INV & NOT & GW -->|트레이스| JAEGER
    INV & NOT -->|로그| OS
    OS --> OSD
```

---

## Kafka 토픽 설계

> 셀프 토픽 없음 — 서비스 간 통신에만 Kafka 사용, 서비스 내부 흐름은 직접 처리

```mermaid
graph LR
    INV[talaria-invest]
    NOT[talaria-notify]

    INV -->|produce| T1[talaria.stock.analyzed<br/>AI 분석 완료 이벤트]
    T1 -->|consume| NOT

    NOT -->|produce| T2[talaria.notification.result<br/>발송 결과]
    NOT -->|produce| T3[talaria.notification.dlq<br/>Dead Letter Queue]

    T2 -->|consume| OS[OpenSearch Sink<br/>Kafka Connect]
    T3 -->|consume| RETRY[Retry Consumer<br/>최대 3회 재시도]
```

| 토픽 | Producer | Consumer | 목적 |
|------|----------|----------|------|
| `talaria.stock.analyzed` | talaria-invest | talaria-notify | 분석 완료 → 알림 트리거 |
| `talaria.notification.result` | talaria-notify | OpenSearch Sink | 발송 결과 저장·분석 |
| `talaria.notification.dlq` | talaria-notify | Retry Consumer | 실패 메시지 재처리 |

---

## 모노레포 서비스 구성

| 서비스 | 포트 | 역할 |
|--------|------|------|
| talaria-eureka | 8761 | 서비스 디스커버리 |
| talaria-gateway | 8080 | API 단일 진입점 |
| talaria-invest | 8081 | 배당주 수집 · 분석 · AI 추천 |
| talaria-notify | 8082 | 다채널 알림 발송 |
| talaria-admin | 3000 | React 관리자 UI |

## 인프라 포트 구성

| 인프라 | 포트 | 역할 |
|--------|------|------|
| MySQL | 3306 | 운영 데이터베이스 |
| Redis | 6379 | 캐시 · Rate Limit |
| Kafka | 9092 | 이벤트 스트리밍 |
| Kafka UI | 8090 | Kafka 관리 콘솔 |
| OpenSearch | 9200 | 로그 · 검색 · 분석 |
| OpenSearch Dashboards | 5601 | 로그 시각화 |
| Prometheus | 9090 | 메트릭 수집 |
| Grafana | 3001 | 메트릭 대시보드 |
| Jaeger UI | 16686 | 분산 트레이싱 |

---

## 기술 스택 요약

| 분류 | 기술 |
|------|------|
| 언어 | Java 25 |
| 프레임워크 | Spring Boot 4.x · Spring Cloud |
| 빌드 | Gradle Multi-project (Kotlin DSL) |
| 패키지 | `io.github.snuggle.talaria` |
| 배포 | Docker Compose |
| 동시성 | Virtual Threads (Project Loom) |
