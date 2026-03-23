package io.snuggle.talaria.invest.adapter.in.web;

import io.snuggle.talaria.common.dto.ApiResponse;
import io.snuggle.talaria.invest.application.port.in.CollectStocksUseCase;
import io.snuggle.talaria.invest.application.port.in.GetStocksQuery;
import io.snuggle.talaria.invest.domain.model.Stock;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invest/stocks")
public class StockController {

    private final GetStocksQuery getStocksQuery;
    private final CollectStocksUseCase collectStocksUseCase;

    public StockController(GetStocksQuery getStocksQuery, CollectStocksUseCase collectStocksUseCase) {
        this.getStocksQuery = getStocksQuery;
        this.collectStocksUseCase = collectStocksUseCase;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Stock>>> getAllStocks() {
        List<Stock> stocks = getStocksQuery.getAllActiveStocks();
        return ResponseEntity.ok(ApiResponse.ok(stocks));
    }

    @GetMapping("/{ticker}")
    public ResponseEntity<ApiResponse<Stock>> getStock(@PathVariable String ticker) {
        return getStocksQuery.getStockByTicker(ticker)
                .map(stock -> ResponseEntity.ok(ApiResponse.ok(stock)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/collect")
    public ResponseEntity<ApiResponse<Integer>> collectStocks() {
        int count = collectStocksUseCase.collectDividendStocks();
        return ResponseEntity.ok(ApiResponse.ok(count));
    }

    @PostMapping("/{ticker}/refresh")
    public ResponseEntity<ApiResponse<Stock>> refreshStock(@PathVariable String ticker) {
        Stock stock = collectStocksUseCase.refreshStockInfo(ticker);
        return ResponseEntity.ok(ApiResponse.ok(stock));
    }
}
