package io.snuggle.talaria.invest.domain.model;

import io.snuggle.talaria.invest.domain.vo.Grade;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Recommendation {
    private Long id;
    private String ticker;
    private String stockName;
    private Grade grade;
    private BigDecimal expectedYield;
    private BigDecimal targetPrice;
    private LocalDate recommendationDate;
    private String summary;
    private boolean notified;

    public Recommendation() {}

    public Recommendation(String ticker, String stockName, Grade grade,
                          BigDecimal expectedYield, BigDecimal targetPrice,
                          LocalDate recommendationDate, String summary) {
        this.ticker = ticker;
        this.stockName = stockName;
        this.grade = grade;
        this.expectedYield = expectedYield;
        this.targetPrice = targetPrice;
        this.recommendationDate = recommendationDate;
        this.summary = summary;
        this.notified = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public String getStockName() { return stockName; }
    public void setStockName(String stockName) { this.stockName = stockName; }

    public Grade getGrade() { return grade; }
    public void setGrade(Grade grade) { this.grade = grade; }

    public BigDecimal getExpectedYield() { return expectedYield; }
    public void setExpectedYield(BigDecimal expectedYield) { this.expectedYield = expectedYield; }

    public BigDecimal getTargetPrice() { return targetPrice; }
    public void setTargetPrice(BigDecimal targetPrice) { this.targetPrice = targetPrice; }

    public LocalDate getRecommendationDate() { return recommendationDate; }
    public void setRecommendationDate(LocalDate recommendationDate) { this.recommendationDate = recommendationDate; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public boolean isNotified() { return notified; }
    public void setNotified(boolean notified) { this.notified = notified; }
}
