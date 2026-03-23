package io.snuggle.talaria.common.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record StockAnalyzedEvent(
        String eventId,
        LocalDate analysisDate,
        List<AnalyzedStock> stocks
) {
    public record AnalyzedStock(
            String ticker,
            String name,
            String market,
            BigDecimal currentPrice,
            String grade,
            BigDecimal expectedYield,
            String summary
    ) {}
}
