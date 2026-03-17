package io.github.snuggle.talaria.invest.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DividendInfo {
    private Long id;
    private String ticker;
    private BigDecimal dividendPerShare;
    private BigDecimal dividendYield;
    private LocalDate exDividendDate;
    private LocalDate paymentDate;
    private String dividendType; // QUARTERLY, SEMI_ANNUAL, ANNUAL
    private int year;
    private int quarter;

    public DividendInfo() {}

    public DividendInfo(String ticker, BigDecimal dividendPerShare, BigDecimal dividendYield,
                        LocalDate exDividendDate, String dividendType) {
        this.ticker = ticker;
        this.dividendPerShare = dividendPerShare;
        this.dividendYield = dividendYield;
        this.exDividendDate = exDividendDate;
        this.dividendType = dividendType;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public BigDecimal getDividendPerShare() { return dividendPerShare; }
    public void setDividendPerShare(BigDecimal dividendPerShare) { this.dividendPerShare = dividendPerShare; }

    public BigDecimal getDividendYield() { return dividendYield; }
    public void setDividendYield(BigDecimal dividendYield) { this.dividendYield = dividendYield; }

    public LocalDate getExDividendDate() { return exDividendDate; }
    public void setExDividendDate(LocalDate exDividendDate) { this.exDividendDate = exDividendDate; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public String getDividendType() { return dividendType; }
    public void setDividendType(String dividendType) { this.dividendType = dividendType; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getQuarter() { return quarter; }
    public void setQuarter(int quarter) { this.quarter = quarter; }
}
