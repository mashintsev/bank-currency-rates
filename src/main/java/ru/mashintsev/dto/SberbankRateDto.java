package ru.mashintsev.dto;

import lombok.Data;

import java.util.Map;

/**
 * Created by i.mashintsev on 20.02.17.
 */
@Data
public class SberbankRateDto {
    private Map<String, Map<String, Rate>> beznal;

    @Data
    public static class Rate {
        private String isoCur;
        private double buyValue;
        private double sellValue;
    }
}
