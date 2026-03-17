# Talaria — API 명세

> Base URL: `http://localhost:8080` (talaria-gateway)
> 인증: `Authorization: Bearer {JWT}` (로그인 후 발급)
> Content-Type: `application/json`

---

## 공통 응답 형식

```json
// 성공
{
  "success": true,
  "data": { ... },
  "message": null,
  "errorCode": null
}

// 실패
{
  "success": false,
  "data": null,
  "message": "종목을 찾을 수 없습니다.",
  "errorCode": "INVEST_001"
}
```

## 공통 에러 코드

| errorCode | 설명 |
|-----------|------|
| `COMMON_001` | 잘못된 입력값 |
| `COMMON_002` | 서버 내부 오류 |
| `INVEST_001` | 종목 없음 |
| `INVEST_002` | KIS API 오류 |
| `INVEST_003` | LLM 분석 오류 |
| `NOTIFY_001` | 알림 없음 |
| `NOTIFY_002` | 채널 발송 오류 |
| `NOTIFY_003` | 채널 사용 불가 |

---

## 1. Auth (인증)

### POST /api/auth/login
JWT 발급. 인증 불필요.

**Request**
```json
{
  "username": "admin",
  "password": "password"
}
```

**Response**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGci...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  }
}
```

---

## 2. Invest — 종목 (Stocks)

### GET /api/invest/stocks
종목 목록 조회 (페이징)

**Query Parameters**
| 파라미터 | 타입 | 기본값 | 설명 |
|---------|------|--------|------|
| `page` | int | 0 | 페이지 번호 |
| `size` | int | 20 | 페이지 크기 |
| `market` | string | - | 시장 필터: `KOSPI`, `KOSDAQ`, `NYSE`, `NASDAQ` |
| `active` | boolean | true | 활성 종목만 조회 |
| `q` | string | - | 종목명/티커 검색 |

**Response**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "ticker": "005930",
        "market": "KOSPI",
        "name": "삼성전자",
        "currentPrice": 72000.00,
        "dividendYield": 0.0350,
        "consecutiveDivYears": 20,
        "isActive": true
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 50,
    "totalPages": 3
  }
}
```

---

### GET /api/invest/stocks/{ticker}
종목 상세 조회

**Path Variable**: `ticker` — 종목 코드 (예: `005930`)

**Response**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "ticker": "005930",
    "market": "KOSPI",
    "name": "삼성전자",
    "englishName": "Samsung Electronics",
    "sector": "반도체",
    "currency": "KRW",
    "currentPrice": 72000.00,
    "marketCap": 430000000000000,
    "annualDividend": 1444.00,
    "dividendYield": 0.0200,
    "dividendPayMonth": "4",
    "consecutiveDivYears": 20,
    "isActive": true,
    "listedDate": "1975-06-11",
    "recentDividends": [
      {
        "fiscalYear": 2024,
        "quarter": 0,
        "dividendPerShare": 1444.00,
        "dividendYield": 0.0200,
        "paymentDate": "2025-04-15"
      }
    ]
  }
}
```

---

### POST /api/invest/stocks
종목 수동 등록

**Request**
```json
{
  "ticker": "005930",
  "market": "KOSPI",
  "name": "삼성전자"
}
```

**Response**: 등록된 종목 상세 (위와 동일 구조)

---

## 3. Invest — 분석 (Analysis)

### GET /api/invest/analysis
오늘의 분석 결과 목록

**Query Parameters**
| 파라미터 | 타입 | 기본값 | 설명 |
|---------|------|--------|------|
| `date` | string | 오늘 | 분석일 (yyyy-MM-dd) |
| `grade` | string | - | 등급 필터: `STRONG_BUY`, `BUY`, `HOLD`, `SELL` |
| `page` | int | 0 | 페이지 번호 |
| `size` | int | 20 | 페이지 크기 |

**Response**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "ticker": "005930",
        "stockName": "삼성전자",
        "analysisDate": "2026-03-15",
        "grade": "BUY",
        "score": 78.50,
        "targetYield": 0.0280,
        "summary": "안정적인 배당 성향과 반도체 업황 회복으로 매수 적합",
        "modelName": "gpt-4o"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 45,
    "totalPages": 3
  }
}
```

---

### GET /api/invest/analysis/{ticker}
종목별 분석 이력

**Path Variable**: `ticker`

**Query Parameters**
| 파라미터 | 타입 | 기본값 | 설명 |
|---------|------|--------|------|
| `from` | string | 30일 전 | 조회 시작일 (yyyy-MM-dd) |
| `to` | string | 오늘 | 조회 종료일 (yyyy-MM-dd) |

**Response**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "analysisDate": "2026-03-15",
      "grade": "BUY",
      "score": 78.50,
      "targetYield": 0.0280,
      "summary": "...",
      "detailJson": {
        "dividendStability": 85,
        "dividendGrowth": 70,
        "payoutRatio": 75,
        "financialHealth": 80
      },
      "modelName": "gpt-4o",
      "latencyMs": 1240
    }
  ]
}
```

---

## 4. Invest — 추천 (Recommendations)

### GET /api/invest/recommendations
현재 유효한 추천 종목 목록

**Query Parameters**
| 파라미터 | 타입 | 기본값 | 설명 |
|---------|------|--------|------|
| `grade` | string | - | 등급 필터 |
| `size` | int | 10 | 조회 개수 |

**Response**
```json
{
  "success": true,
  "data": [
    {
      "ticker": "005930",
      "stockName": "삼성전자",
      "grade": "BUY",
      "reasoning": "연속 20년 배당, 업황 회복 기대",
      "targetYield": 0.0280,
      "validUntil": "2026-03-16",
      "analysisDate": "2026-03-15"
    }
  ]
}
```

---

## 5. Invest — 스케줄러 수동 실행 (Admin)

### POST /api/invest/scheduler/collect
데이터 수집 수동 트리거

**Response**
```json
{
  "success": true,
  "data": {
    "scheduleLogId": 42,
    "status": "STARTED",
    "startedAt": "2026-03-15T08:00:00"
  }
}
```

---

### POST /api/invest/scheduler/analyze
AI 분석 수동 트리거

**Response**
```json
{
  "success": true,
  "data": {
    "scheduleLogId": 43,
    "status": "STARTED",
    "startedAt": "2026-03-15T08:01:00"
  }
}
```

---

### GET /api/invest/scheduler/logs
스케줄러 실행 이력

**Query Parameters**
| 파라미터 | 타입 | 기본값 | 설명 |
|---------|------|--------|------|
| `page` | int | 0 | 페이지 번호 |
| `size` | int | 10 | 페이지 크기 |

**Response**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 42,
        "jobName": "COLLECT",
        "status": "SUCCESS",
        "totalCount": 50,
        "successCount": 50,
        "failCount": 0,
        "startedAt": "2026-03-15T08:00:00",
        "completedAt": "2026-03-15T08:01:23",
        "durationMs": 83000
      }
    ]
  }
}
```

---

## 6. Notify — 알림 발송

### POST /api/notify/send
수동 알림 발송

**Request**
```json
{
  "title": "긴급 알림",
  "content": "삼성전자 배당 공시",
  "channels": ["SMS", "SLACK", "TELEGRAM", "KAKAO"]
}
```

**Response**
```json
{
  "success": true,
  "data": {
    "notificationId": 101,
    "channels": ["SMS", "SLACK", "TELEGRAM", "KAKAO"],
    "status": "PROCESSING"
  }
}
```

---

## 7. Notify — 발송 이력

### GET /api/notify/notifications
발송 이력 목록

**Query Parameters**
| 파라미터 | 타입 | 기본값 | 설명 |
|---------|------|--------|------|
| `page` | int | 0 | 페이지 번호 |
| `size` | int | 20 | 페이지 크기 |
| `from` | string | 7일 전 | 조회 시작일 |
| `to` | string | 오늘 | 조회 종료일 |

**Response**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 101,
        "title": "오늘의 배당주 분석 결과",
        "sourceType": "STOCK_ANALYZED",
        "priority": "NORMAL",
        "channels": ["SMS", "SLACK", "TELEGRAM", "KAKAO"],
        "createdAt": "2026-03-15T08:02:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 120,
    "totalPages": 6
  }
}
```

---

### GET /api/notify/notifications/{id}
발송 상세 + 채널별 결과

**Path Variable**: `id`

**Response**
```json
{
  "success": true,
  "data": {
    "id": 101,
    "title": "오늘의 배당주 분석 결과",
    "content": "[Talaria] 2026-03-15 배당주 분석 결과...",
    "sourceType": "STOCK_ANALYZED",
    "priority": "NORMAL",
    "createdAt": "2026-03-15T08:02:00",
    "dispatches": [
      {
        "channel": "SMS",
        "status": "SENT",
        "externalMessageId": "uuid-1234",
        "retryCount": 0,
        "sentAt": "2026-03-15T08:02:01"
      },
      {
        "channel": "SLACK",
        "status": "SENT",
        "externalMessageId": "uuid-5678",
        "retryCount": 0,
        "sentAt": "2026-03-15T08:02:01"
      },
      {
        "channel": "TELEGRAM",
        "status": "FAILED",
        "externalMessageId": null,
        "retryCount": 3,
        "errorMessage": "connection timeout",
        "sentAt": null
      }
    ]
  }
}
```

---

## 8. Notify — 채널 상태

### GET /api/notify/channels
채널 활성화 상태 조회

**Response**
```json
{
  "success": true,
  "data": [
    { "channel": "SMS",      "available": true  },
    { "channel": "KAKAO",    "available": true  },
    { "channel": "TELEGRAM", "available": true  },
    { "channel": "SLACK",    "available": true  }
  ]
}
```

---

## 9. Health

### GET /actuator/health
서비스 헬스체크. 인증 불필요.

**Response**
```json
{ "status": "UP" }
```
