package ru.mashintsev.dto;

import lombok.Data;

/**
 * Created by i.mashintsev on 20.02.17.
 */
@Data
public class RsbRatesDto {
    private Course courses;

    @Data
    public static class Course {
        private String sections_dol_rate;
        private String sections_dol_sell;
        private String sections_eur_rate;
        private String sections_eur_sell;
    }
}
