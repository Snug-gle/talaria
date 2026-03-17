package io.github.snuggle.talaria.invest.domain.vo;

public enum Grade {
    STRONG_BUY("강력 매수"),
    BUY("매수"),
    HOLD("보유"),
    SELL("매도");

    private final String description;

    Grade(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
