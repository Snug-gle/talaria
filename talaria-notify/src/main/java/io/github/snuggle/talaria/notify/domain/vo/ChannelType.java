package io.github.snuggle.talaria.notify.domain.vo;

public enum ChannelType {
    SMS("SMS 시뮬레이터"),
    KAKAO("카카오톡"),
    TELEGRAM("텔레그램"),
    SLACK("슬랙");

    private final String description;

    ChannelType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
