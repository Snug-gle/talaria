# Talaria — 스택 선택 이유

각 기술을 선택한 이유와 Talaria에서의 역할을 정리합니다.

---

## Java 25 + Spring Boot 4.x

### 왜 Java 25?
- 2025년 9월 출시된 LTS 버전
- **Virtual Threads (Project Loom)** 가 정식 지원 → Reactor/WebFlux 없이 고동시성 처리
- Record, Sealed Class, Pattern Matching 등 현대적 문법 완성
- Spring Boot 4.x의 공식 지원 버전

### Talaria에서의 역할
```
Kafka 메시지 소비 → Virtual Thread로 다채널 병렬 발송
외부 API 호출    → Virtual Thread로 50개 종목 동시 수집
```

### Virtual Threads란?
```
기존 (플랫폼 스레드)          Virtual Thread
────────────────────         ─────────────────
스레드 수백 개 한계           수십만 개 생성 가능
1 스레드 = OS 스레드 1개      경량 스레드, JVM이 관리
I/O 대기 시 스레드 점유        I/O 대기 시 반환, 다른 작업 처리
```

---

## Spring Cloud Gateway

### 왜 Gateway?
MSA에서 클라이언트가 서비스마다 주소를 알면 문제가 생깁니다.

```
Gateway 없는 경우              Gateway 있는 경우
──────────────────             ──────────────────
클라이언트                      클라이언트
  ├── invest:8081                    │
  ├── notify:8082              gateway:8080
  └── ...                       ├── /api/invest/** → invest
  → 서비스 추가 시 클라이언트      └── /api/notify/** → notify
    코드 변경 필요               → 클라이언트는 8080만 알면 됨
```

### Talaria에서 처리하는 것
- **API Key 인증**: 외부 서비스가 Talaria를 호출할 때 검증
- **JWT 인증**: 관리자 UI 로그인 토큰 검증
- **Rate Limiting**: 과도한 요청 차단
- **라우팅**: URL 기반으로 각 서비스에 전달

---

## Spring Cloud Eureka

### 왜 Eureka?
Docker 환경에서 서비스의 IP는 컨테이너 재시작 때마다 바뀝니다.

```
Eureka 없는 경우               Eureka 있는 경우
──────────────────             ──────────────────
gateway가 invest 주소를         invest가 시작 시
application.yml에 하드코딩       Eureka에 자동 등록
→ IP 바뀌면 설정 변경 필요       gateway는 "invest 어디있어?"
                               → Eureka가 알려줌
```

---

## Apache Kafka (KRaft Mode)

### 왜 Kafka?
invest와 notify를 REST로 연결하면 결합도가 생깁니다.

```
REST 방식 (문제)
invest → POST /notify/send → notify
→ notify 서버 다운 시 알림 유실
→ invest가 notify 응답 기다리는 동안 블로킹

Kafka 방식 (해결)
invest → Kafka(stock.analyzed) → notify
→ notify 다운돼도 메시지 Kafka에 보관
→ notify 살아나면 자동으로 처리
→ invest는 Kafka에 던지고 바로 다음 작업
```

### KRaft Mode란?
Kafka 4.0부터 Zookeeper가 완전히 제거된 모드입니다.
```
기존: Kafka + Zookeeper (2개 운영)
KRaft: Kafka만 (1개 운영)
→ Docker Compose 단순화
→ 2025년 실무 표준
```

### Talaria의 토픽 구조

> **설계 원칙**: 셀프 토픽 없음 — Kafka는 서비스 간 통신에만 사용, 서비스 내부 처리 흐름은 직접 구현

| 토픽 | Producer | Consumer | 목적 |
|------|----------|----------|------|
| `talaria.stock.analyzed` | talaria-invest | talaria-notify | 분석 완료 → 알림 트리거 |
| `talaria.notification.result` | talaria-notify | OpenSearch Sink | 발송 결과 저장·분석 |
| `talaria.notification.dlq` | talaria-notify | Retry Consumer | 실패 메시지 재처리 |

### 배울 수 있는 실무 패턴
- Producer / Consumer 기본
- Consumer Group (병렬 처리)
- Dead Letter Queue (장애 처리)
- Kafka Connect (OpenSearch Sink)
- KRaft Mode 운영

---

## MySQL 8.0

### 왜 MySQL?
- 관계형 데이터 (종목 ↔ 배당이력 ↔ 분석결과)에 최적
- 알림 발송 이력 등 **사라지면 안 되는 데이터** 영구 저장
- Spring Data JPA와 자연스러운 통합
- 실무에서 가장 많이 쓰이는 RDBMS

### Talaria에서 저장하는 것
```
talaria-invest DB
  ├── 종목 정보 (stocks)
  ├── 배당 이력 (dividend_history)
  ├── AI 분석 결과 (analysis_results)
  └── 추천 기록 (recommendations)

talaria-notify DB
  ├── 알림 마스터 (notifications)
  ├── 채널별 발송 단위 (notification_dispatches)
  └── 채널 설정 (channel_configs)
```

---

## Redis 7

### 왜 Redis?
매번 MySQL에 조회하면 느립니다.

```
시나리오: 오늘의 배당주 추천 결과 조회
MySQL만 사용   → 매 요청마다 DB 조회 (느림)
Redis 캐싱    → 첫 조회만 DB, 이후는 메모리 (빠름)
```

### Talaria에서 사용하는 것
- 오늘의 분석 결과 캐싱 (TTL 1시간)
- API Key 검증 결과 캐싱
- Gateway Rate Limit 카운터
- Eureka 레지스트리 캐싱

---

## OpenSearch

### 왜 Elasticsearch 대신 OpenSearch?
| | Elasticsearch | OpenSearch |
|--|--|--|
| 라이선스 | SSPL (제한적, 사실상 유료) | Apache 2.0 (완전 무료) |
| 운영 비용 | 클라우드 유료 서비스 강제 | 로컬 완전 무료 |
| API 호환성 | - | ES 7.10 호환 |
| AWS 환경 | - | AWS 공식 지원 |

2025년 기준 오픈소스 프로젝트에서 OpenSearch가 표준으로 자리잡았습니다.

### Talaria에서 사용하는 것

**1. 로그 집계 (ELK 대신 EOS 스택)**
```
서비스 로그 → Logback → Kafka → OpenSearch → Dashboards
"10개 서비스 로그를 한 곳에서 검색"
"에러 발생 시 전체 흐름을 텍스트로 추적"
```

**2. 알림 발송 분석**
```
Kafka notification.result → OpenSearch
→ 채널별 성공률 시각화
→ 시간대별 발송량 분석
→ 실패 패턴 분석
```

**3. 종목 검색**
```
"삼성" 입력 → OpenSearch → 삼성전자, 삼성SDI, 삼성물산...
MySQL LIKE 보다 빠르고 정확한 풀텍스트 검색
```

---

## Prometheus + Grafana

### 역할 구분
```
Prometheus  =  숫자(메트릭)를 주기적으로 수집해서 저장
Grafana     =  그 숫자를 그래프로 시각화
```

### Talaria에서 수집하는 메트릭

| 대상 | 메트릭 예시 |
|------|------------|
| 각 서비스 (JVM) | 메모리 사용량, GC 횟수, 스레드 수 |
| Spring Boot | HTTP 요청 수, 응답 시간, 에러율 |
| Kafka | Consumer Lag, 초당 메시지 처리량 |
| MySQL | 쿼리 응답 시간, 커넥션 수 |
| Redis | 히트율, 메모리 사용량 |

### 왜 이게 중요한가?
```
"오전 9시에 CPU 스파이크가 발생한다"
→ 스케줄러가 50개 종목을 동시에 분석하기 때문
→ Virtual Thread 조정 필요

"Kafka Consumer Lag가 계속 늘어난다"
→ notify 서비스 처리 속도가 발송 속도를 못 따라감
→ Consumer 인스턴스 증설 필요
```

---

## Jaeger (분산 트레이싱)

### 왜 필요한가?
MSA에서 하나의 요청이 여러 서비스를 거칩니다.

```
요청: "오늘 배당주 추천 알림 발송"

gateway (2ms)
  → invest (1.2s)  ← 여기서 느렸네
    → KIS API (1.1s)  ← 외부 API가 느린 거였어
  → kafka (5ms)
  → notify (150ms)
    → Slack API (130ms)  ← 슬랙도 좀 느리네
```

로그만으로는 어느 구간이 느린지 알 수 없습니다.
Jaeger는 각 구간의 소요 시간을 **시각적으로** 보여줍니다.

### OpenTelemetry와의 관계
```
OpenTelemetry  =  트레이싱 데이터를 수집하는 표준 규격
Jaeger         =  그 데이터를 저장하고 시각화하는 도구

코드에서는 OpenTelemetry만 사용
→ Jaeger 대신 다른 도구로 교체 가능 (Zipkin, Tempo 등)
```

---

## Spring AI

### 왜 Spring AI?
직접 OpenAI API를 호출하면 특정 모델에 종속됩니다.

```
직접 호출 방식 (문제)
OpenAiClient.call(prompt)
→ OpenAI → Claude 교체 시 코드 전면 수정

Spring AI 방식 (해결)
ChatClient.call(prompt)  ← 추상화된 인터페이스
→ application.yml 설정만 바꾸면 모델 교체
```

### Talaria에서의 사용
```
배당주 데이터 + 프롬프트 → LLM → 분석 결과 (JSON)

- 배당 성향 안정성 평가
- 배당 성장률 추세
- 투자 추천 등급 (STRONG_BUY / BUY / HOLD / SELL)
- 예상 배당수익률
```

### 지원 모델 (교체 가능)
```yaml
# application.yml 에서 선택
spring.ai.openai    → GPT-4o
spring.ai.anthropic → Claude Sonnet
spring.ai.ollama    → 로컬 LLM (비용 0원)
```

---

## 채널 어댑터 패턴

### 왜 이 패턴?
채널마다 API 방식이 다릅니다.

```
SMS      → 통신사 REST API
카카오톡  → Kakao API
텔레그램  → Bot API
슬랙     → Webhook / Bot API
```

새 채널을 추가할 때마다 핵심 로직을 수정하면 버그가 생깁니다.

```java
// 이 인터페이스만 구현하면 자동 등록
interface ChannelAdapter {
    ChannelType channel();
    SendResult send(Notification notification);
    boolean isAvailable();
}

// 새 채널 추가 = 구현체 클래스 하나만 작성
// notify 핵심 로직은 수정 없음
```

### Snap 같은 외부 발송 모듈?
기존 linkwave 프로젝트는 Snap이라는 외부 발송 에이전트에 의존했습니다.
Talaria는 **ChannelAdapter로 완전 추상화**하여 어떤 외부 모듈과도 결합하지 않습니다.

---

## 스택 전체 요약

| 스택 | 분류 | Talaria에서의 역할 |
|------|------|-------------------|
| Java 25 | 언어 | Virtual Threads로 고동시성 처리 |
| Spring Boot 4.x | 프레임워크 | 서비스 뼈대 |
| Spring Cloud Gateway | MSA | 단일 진입점, 인증, 라우팅 |
| Spring Cloud Eureka | MSA | 서비스 주소록 |
| Spring AI | AI | LLM 추상화, 배당주 분석 |
| Kafka (KRaft) | 스트리밍 | 서비스 간 비동기 이벤트, 데이터 파이프라인 |
| MySQL 8 | 저장 | 운영 데이터 영구 저장 |
| Redis 7 | 저장 | 캐시, Rate Limit |
| OpenSearch | 저장/검색 | 로그 집계, 검색, 발송 통계 분석 |
| Prometheus | 관측성 | 메트릭 수집 |
| Grafana | 관측성 | 메트릭 시각화 대시보드 |
| Jaeger | 관측성 | 분산 트레이싱, 병목 탐지 |
| OpenTelemetry | 관측성 | 트레이싱 표준 규격 (코드 레벨) |
| Docker Compose | 인프라 | 로컬 전체 환경 단일 실행 |
