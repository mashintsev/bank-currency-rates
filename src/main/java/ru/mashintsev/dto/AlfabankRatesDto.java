package ru.mashintsev.dto;

import lombok.Data;

import java.util.Map;

/**
 * Created by i.mashintsev on 19.02.17.
 * <pre>
 *     {
 *          "request": {
 *              "server": "exchange",
 *              "service": "cash",
 *              "version": "0.2",
 *              "order": "",
 *              "limit": "",
 *              "offset": "",
 *              "filter": {
 *                  "text": "",
 *                  "segment": "misc"
 *              }
 *          },
 *          "response": {
 *              "status": "ok",
 *              "data": {
 *                  "usd": [
 *                      {
 *                          "type": "buy",
 *                          "date": "2017-02-18 01:34:00",
 *                          "value": 57.8,
 *                          "order": "-"
 *                      },
 *                      {
 *                          "type": "sell",
 *                          "date": "2017-02-18 01:34:00",
 *                          "value": 58.8,
 *                          "order": "-"
 *                      }
 *                  ]
 *              }
 *          }
 * }
 * </pre>
 */
@Data
public class AlfabankRatesDto {
    private Response response;

    @Data
    public static class Response {
        private Map<String, Rate[]> data;
    }

    @Data
    public static class Rate {
        private String type;
        private String date;
        private double value;
    }
}
