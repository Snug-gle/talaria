package io.github.snuggle.talaria.common.exception;

public enum ErrorCode {
    // Common
    INVALID_INPUT("COMMON_001", "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR("COMMON_002", "서버 내부 오류가 발생했습니다."),

    // Invest
    STOCK_NOT_FOUND("INVEST_001", "종목을 찾을 수 없습니다."),
    KIS_API_ERROR("INVEST_002", "한국투자증권 API 오류가 발생했습니다."),
    LLM_ANALYSIS_ERROR("INVEST_003", "AI 분석 중 오류가 발생했습니다."),

    // Notify
    NOTIFICATION_NOT_FOUND("NOTIFY_001", "알림을 찾을 수 없습니다."),
    CHANNEL_SEND_ERROR("NOTIFY_002", "채널 발송 중 오류가 발생했습니다."),
    CHANNEL_UNAVAILABLE("NOTIFY_003", "채널을 사용할 수 없습니다.");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
}
