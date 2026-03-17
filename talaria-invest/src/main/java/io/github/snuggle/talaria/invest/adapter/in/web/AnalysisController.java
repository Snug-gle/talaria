package io.github.snuggle.talaria.invest.adapter.in.web;

import io.github.snuggle.talaria.common.dto.ApiResponse;
import io.github.snuggle.talaria.invest.application.port.in.AnalyzeStockUseCase;
import io.github.snuggle.talaria.invest.application.port.in.GetStocksQuery;
import io.github.snuggle.talaria.invest.domain.model.AnalysisResult;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/invest/analysis")
public class AnalysisController {

    private final AnalyzeStockUseCase analyzeStockUseCase;
    private final GetStocksQuery getStocksQuery;

    public AnalysisController(AnalyzeStockUseCase analyzeStockUseCase, GetStocksQuery getStocksQuery) {
        this.analyzeStockUseCase = analyzeStockUseCase;
        this.getStocksQuery = getStocksQuery;
    }

    @PostMapping("/run")
    public ResponseEntity<ApiResponse<List<AnalysisResult>>> runAnalysis() {
        List<AnalysisResult> results = analyzeStockUseCase.analyzeAllStocks();
        analyzeStockUseCase.publishAnalysisResults(LocalDate.now(), results);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    @PostMapping("/{ticker}")
    public ResponseEntity<ApiResponse<AnalysisResult>> analyzeStock(@PathVariable String ticker) {
        AnalysisResult result = analyzeStockUseCase.analyzeStock(ticker);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AnalysisResult>>> getAnalysisByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AnalysisResult> results = getStocksQuery.getAnalysisResultsByDate(date);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    @GetMapping("/{ticker}/latest")
    public ResponseEntity<ApiResponse<AnalysisResult>> getLatestAnalysis(@PathVariable String ticker) {
        return getStocksQuery.getLatestAnalysisResult(ticker)
                .map(result -> ResponseEntity.ok(ApiResponse.ok(result)))
                .orElse(ResponseEntity.notFound().build());
    }
}
