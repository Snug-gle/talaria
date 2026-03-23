package io.snuggle.talaria.invest.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Stock {
    private Long id;
    private String ticker;
    private String name;
    private String market; // KOSPI, KOSDAQ
    private BigDecimal currentPrice;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Stock() {}

    public Stock(String ticker, String name, String market, BigDecimal currentPrice) {
        this.ticker = ticker;
        this.name = name;
        this.market = market;
        this.currentPrice = currentPrice;
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMarket() { return market; }
    public void setMarket(String market) { this.market = market; }

    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
