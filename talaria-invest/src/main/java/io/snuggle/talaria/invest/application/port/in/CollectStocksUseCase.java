package io.snuggle.talaria.invest.application.port.in;

import io.snuggle.talaria.invest.domain.model.Stock;

public interface CollectStocksUseCase {

    /**
     * 시장 데이터로부터 배당주 목록을 수집하고 저장합니다.
     *
     * @return 수집된 종목 수
     */
    int collectDividendStocks();

    /**
     * 특정 종목의 배당 정보를 갱신합니다.
     *
     * @param ticker 종목 코드
     * @return 갱신된 종목
     */
    Stock refreshStockInfo(String ticker);
}
