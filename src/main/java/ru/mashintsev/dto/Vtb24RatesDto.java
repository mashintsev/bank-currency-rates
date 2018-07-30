package ru.mashintsev.dto;

import lombok.Data;

/**
 * Created by i.mashintsev on 17.02.17.
 * <pre>
 *     {
 *          "items": [
 *              {
 *                  "currencyGroupAbbr": "cash-desk",
 *                  "currencyAbbr": "USD",
 *                  "title": "Доллар США",
 *                  "quantity": 1,
 *                  "buy": "57,8500",
 *                  "buyArrow": "Up",
 *                  "sell": "58,8700",
 *                  "sellArrow": "Up",
 *                  "gradation": 250000,
 *                  "dateActiveFrom": "/Date(1487360733726)/",
 *                  "isMetal": false
 *              }
 *          ]
 *      }
 * </pre>
 */
@Data
public class Vtb24RatesDto {
    private VtbRate[] items;

    @Data
    public static class VtbRate {
        private String currencyGroupAbbr;
        private String currencyAbbr;
        private String buy;
        private String sell;
    }
}
