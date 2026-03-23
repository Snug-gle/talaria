package io.snuggle.talaria.invest.application.port.in;

import io.snuggle.talaria.invest.domain.model.AnalysisResult;
import io.snuggle.talaria.invest.domain.model.Stock;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GetStocksQuery {

    /**
     * 모든 활성 종목 목록을 조회합니다.
     *
     * @return 활성 종목 목록
     */
    List<Stock> getAllActiveStocks();

    /**
     * 종목 코드로 종목을 조회합니다.
     *
     * @param ticker 종목 코드
     * @return 종목 (없으면 empty)
     */
    Optional<Stock> getStockByTicker(String ticker);

    /**
     * 특정 날짜의 분석 결과를 조회합니다.
     *
     * @param analysisDate 분석 날짜
     * @return 분석 결과 목록
     */
    List<AnalysisResult> getAnalysisResultsByDate(LocalDate analysisDate);

    /**
     * 특정 종목의 최신 분석 결과를 조회합니다.
     *
     * @param ticker 종목 코드
     * @return 최신 분석 결과 (없으면 empty)
     */
    Optional<AnalysisResult> getLatestAnalysisResult(String ticker);
}
