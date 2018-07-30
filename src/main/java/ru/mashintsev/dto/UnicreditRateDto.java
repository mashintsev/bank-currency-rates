package ru.mashintsev.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Created by i.mashintsev on 20.02.17.
 * <p>
 * {Currency
 * :
 * "RUB"
 * Currency1
 * :
 * "USD"
 * CurrencySmall
 * :
 * "rub"
 * CurrencySmall1
 * :
 * "usd"
 * Date
 * :
 * "2017-02-20"
 * Forex
 * :
 * {NBR: "57.6342", BUY: "56.7", SELL: "59.7"}
 * Unit
 * :
 * 1}
 */
@Data
public class UnicreditRateDto {
    @JsonProperty("Currency")
    private String currency;
    @JsonProperty("Currency1")
    private String currency1;
    @JsonProperty("Date")
    private String date;
    @JsonProperty("Forex")
    private ForexRate forex;

    @Data
    public static class ForexRate {
        @JsonProperty("SELL")
        private double sell;
        @JsonProperty("BUY")
        private double buy;
    }
}
