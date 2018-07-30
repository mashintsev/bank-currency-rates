package ru.mashintsev.dto;

import lombok.Data;
import lombok.ToString;

/**
 * Created by i.mashintsev on 17.02.17.
 * <pre>
 * {
 *      "resultCode": "OK",
 *      "payload": {
 *          "lastUpdate": {
 *              "milliseconds": 1487337616185
 *          },
 *          "rates": [
 *              {
 *                  "category": "DepositClosingBenefit",
 *                  "fromCurrency": {
 *                      "code": 840,
 *                      "name": "USD"
 *                  },
 *                  "toCurrency": {
 *                      "code": 643,
 *                      "name": "RUB"
 *                  },
 *                  "buy": 56.35
 *              }
 *          ]
 *      }
 * }
 * </pre>
 */
@Data
public class TinkoffRatesDto {
    private String resultCode;
    private Payload payload;

    @Data
    public static class Payload {
        private Rate[] rates;
    }

    @Data
    public static class Rate {
        private String category;
        private Currency fromCurrency;
        private Currency toCurrency;
        private double sell;
        private double buy;
    }

    @Data
    public static class Currency {
        private String code;
        private String name;
    }
}
