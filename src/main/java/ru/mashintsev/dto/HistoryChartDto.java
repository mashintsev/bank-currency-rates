package ru.mashintsev.dto;

import java.util.Map;

/**
 * Created by i.mashintsev on 04.03.17.
 */
public class HistoryChartDto {
    private String symbol;
    private long updatedAt;
    private Map<String, ExchageRateDto> bankRates;
}
