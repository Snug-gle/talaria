package io.snuggle.talaria.notify.domain.vo;

public enum Priority {
    HIGH("즉시 처리"),    // 급등/급락 알림
    NORMAL("일반 큐"),   // 배당 공시 알림
    LOW("배치 처리");    // 주간 리포트

    private final String description;

    Priority(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
