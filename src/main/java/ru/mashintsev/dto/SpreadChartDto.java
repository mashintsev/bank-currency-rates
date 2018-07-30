package ru.mashintsev.dto;

import lombok.Data;
import ru.mashintsev.data.Bank;
import ru.mashintsev.data.ExchangeRate;

import java.util.function.Function;

/**
 * Created by i.mashintsev on 17.02.17.
 */
@Data
public class SpreadChartDto {
    private String name;
    private double value;

    public static Function<ExchangeRate, SpreadChartDto> map = er -> {
        SpreadChartDto dto = new SpreadChartDto();
        dto.name = Bank.valueOf(er.getBank()).label;
        dto.value = (double) Math.round((er.getOffer() - er.getBid()) * 100d) / 100;
        return dto;
    };
}
