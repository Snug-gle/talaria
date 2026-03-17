package io.github.snuggle.talaria.invest.application.port.in;

import io.github.snuggle.talaria.invest.domain.model.AnalysisResult;

import java.time.LocalDate;
import java.util.List;

public interface AnalyzeStockUseCase {

    /**
     * 모든 활성 배당주를 AI로 분석하고 추천 등급을 산출합니다.
     *
     * @return 분석된 결과 목록
     */
    List<AnalysisResult> analyzeAllStocks();

    /**
     * 특정 종목을 AI로 분석합니다.
     *
     * @param ticker 종목 코드
     * @return 분석 결과
     */
    AnalysisResult analyzeStock(String ticker);

    /**
     * 분석 결과를 이벤트로 발행합니다.
     *
     * @param analysisDate 분석 날짜
     * @param results      분석 결과 목록
     */
    void publishAnalysisResults(LocalDate analysisDate, List<AnalysisResult> results);
}
