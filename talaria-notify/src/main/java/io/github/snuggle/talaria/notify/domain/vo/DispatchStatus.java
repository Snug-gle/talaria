package io.github.snuggle.talaria.notify.domain.vo;

public enum DispatchStatus {
    PENDING,    // 발송 대기
    SENDING,    // 채널 API 호출 중
    SENT,       // API 호출 성공
    DELIVERED,  // 수신 확인
    FAILED,     // API 호출 실패
    RETRY,      // 재시도 중 (최대 3회, 지수 백오프)
    DLQ         // 최종 실패 → Dead Letter Queue
}
