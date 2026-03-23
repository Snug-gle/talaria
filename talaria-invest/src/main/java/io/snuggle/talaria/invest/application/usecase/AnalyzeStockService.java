package io.snuggle.talaria.invest.application.usecase;

import io.snuggle.talaria.common.event.StockAnalyzedEvent;
import io.snuggle.talaria.invest.application.port.in.AnalyzeStockUseCase;
import io.snuggle.talaria.invest.application.port.out.AnalyzeStockPort;
import io.snuggle.talaria.invest.application.port.out.LoadStockPort;
import io.snuggle.talaria.invest.application.port.out.PublishStockEventPort;
import io.snuggle.talaria.invest.application.port.out.SaveStockPort;
import io.snuggle.talaria.invest.domain.model.AnalysisResult;
import io.snuggle.talaria.invest.domain.model.DividendInfo;
import io.snuggle.talaria.invest.domain.model.Stock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AnalyzeStockService implements AnalyzeStockUseCase {

    private static final Logger log = LoggerFactory.getLogger(AnalyzeStockService.class);

    private final LoadStockPort loadStockPort;
    private final SaveStockPort saveStockPort;
    private final AnalyzeStockPort analyzeStockPort;
    private final PublishStockEventPort publishStockEventPort;

    public AnalyzeStockService(LoadStockPort loadStockPort,
                               SaveStockPort saveStockPort,
                               AnalyzeStockPort analyzeStockPort,
                               PublishStockEventPort publishStockEventPort) {
        this.loadStockPort = loadStockPort;
        this.saveStockPort = saveStockPort;
        this.analyzeStockPort = analyzeStockPort;
        this.publishStockEventPort = publishStockEventPort;
    }

    @Override
    public List<AnalysisResult> analyzeAllStocks() {
        log.info("Starting AI analysis for all active stocks...");
        List<Stock> stocks = loadStockPort.findAllActiveStocks();
        List<AnalysisResult> results = new ArrayList<>();

        for (Stock stock : stocks) {
            try {
                AnalysisResult result = analyzeStock(stock.getTicker());
                results.add(result);
            } catch (Exception e) {
                log.error("Failed to analyze stock ticker={}", stock.getTicker(), e);
            }
        }

        log.info("Analysis completed for {} stocks", results.size());
        return results;
    }

    @Override
    public AnalysisResult analyzeStock(String ticker) {
        log.info("Analyzing stock ticker={}", ticker);
        Stock stock = loadStockPort.findByTicker(ticker)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found: " + ticker));
        List<DividendInfo> dividendInfos = loadStockPort.findDividendInfoByTicker(ticker);

        AnalysisResult result = analyzeStockPort.analyze(stock, dividendInfos);
        return saveStockPort.saveAnalysisResult(result);
    }

    @Override
    public void publishAnalysisResults(LocalDate analysisDate, List<AnalysisResult> results) {
        List<StockAnalyzedEvent.AnalyzedStock> analyzedStocks = results.stream()
                .map(r -> new StockAnalyzedEvent.AnalyzedStock(
                        r.getTicker(),
                        loadStockPort.findByTicker(r.getTicker()).map(Stock::getName).orElse(""),
                        loadStockPort.findByTicker(r.getTicker()).map(Stock::getMarket).orElse(""),
                        loadStockPort.findByTicker(r.getTicker()).map(Stock::getCurrentPrice).orElse(null),
                        r.getGrade().name(),
                        r.getExpectedYield(),
                        r.getSummary()
                ))
                .toList();

        StockAnalyzedEvent event = new StockAnalyzedEvent(
                UUID.randomUUID().toString(),
                analysisDate,
                analyzedStocks
        );

        publishStockEventPort.publishStockAnalyzedEvent(event);
        log.info("Published StockAnalyzedEvent | eventId={} | stockCount={}", event.eventId(), analyzedStocks.size());
    }
}
