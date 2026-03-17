package io.github.snuggle.talaria.invest.application.usecase;

import io.github.snuggle.talaria.invest.application.port.in.CollectStocksUseCase;
import io.github.snuggle.talaria.invest.application.port.out.FetchMarketDataPort;
import io.github.snuggle.talaria.invest.application.port.out.SaveStockPort;
import io.github.snuggle.talaria.invest.domain.model.DividendInfo;
import io.github.snuggle.talaria.invest.domain.model.Stock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CollectStocksService implements CollectStocksUseCase {

    private static final Logger log = LoggerFactory.getLogger(CollectStocksService.class);

    private final FetchMarketDataPort fetchMarketDataPort;
    private final SaveStockPort saveStockPort;

    public CollectStocksService(FetchMarketDataPort fetchMarketDataPort, SaveStockPort saveStockPort) {
        this.fetchMarketDataPort = fetchMarketDataPort;
        this.saveStockPort = saveStockPort;
    }

    @Override
    @Scheduled(cron = "${talaria.invest.scheduler.cron:0 0 8 * * MON-FRI}")
    public int collectDividendStocks() {
        log.info("Starting dividend stock collection...");
        List<Stock> stocks = fetchMarketDataPort.fetchDividendStocks();

        for (Stock stock : stocks) {
            try {
                List<DividendInfo> dividendInfos = fetchMarketDataPort.fetchDividendInfo(stock.getTicker());
                dividendInfos.forEach(saveStockPort::saveDividendInfo);
            } catch (Exception e) {
                log.warn("Failed to fetch dividend info for ticker={}", stock.getTicker(), e);
            }
        }

        List<Stock> saved = saveStockPort.saveAllStocks(stocks);
        log.info("Collected {} dividend stocks", saved.size());
        return saved.size();
    }

    @Override
    public Stock refreshStockInfo(String ticker) {
        log.info("Refreshing stock info for ticker={}", ticker);
        Stock updated = fetchMarketDataPort.fetchCurrentPrice(ticker);
        List<DividendInfo> dividendInfos = fetchMarketDataPort.fetchDividendInfo(ticker);
        dividendInfos.forEach(saveStockPort::saveDividendInfo);
        return saveStockPort.saveStock(updated);
    }
}
