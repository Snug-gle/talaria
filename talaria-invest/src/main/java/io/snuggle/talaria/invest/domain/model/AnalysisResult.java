package io.snuggle.talaria.invest.domain.model;

import io.snuggle.talaria.invest.domain.vo.Grade;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AnalysisResult {
    private Long id;
    private String ticker;
    private LocalDate analysisDate;
    private Grade grade;
    private BigDecimal expectedYield;
    private BigDecimal targetPrice;
    private String summary;
    private String reasoning;
    private String modelVersion;
    private LocalDateTime createdAt;

    public AnalysisResult() {}

    public AnalysisResult(String ticker, LocalDate analysisDate, Grade grade,
                          BigDecimal expectedYield, BigDecimal targetPrice, String summary) {
        this.ticker = ticker;
        this.analysisDate = analysisDate;
        this.grade = grade;
        this.expectedYield = expectedYield;
        this.targetPrice = targetPrice;
        this.summary = summary;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public LocalDate getAnalysisDate() { return analysisDate; }
    public void setAnalysisDate(LocalDate analysisDate) { this.analysisDate = analysisDate; }

    public Grade getGrade() { return grade; }
    public void setGrade(Grade grade) { this.grade = grade; }

    public BigDecimal getExpectedYield() { return expectedYield; }
    public void setExpectedYield(BigDecimal expectedYield) { this.expectedYield = expectedYield; }

    public BigDecimal getTargetPrice() { return targetPrice; }
    public void setTargetPrice(BigDecimal targetPrice) { this.targetPrice = targetPrice; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
