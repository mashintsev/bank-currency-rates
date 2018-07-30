package ru.mashintsev.dto;

import lombok.Data;
import ru.mashintsev.data.Bank;
import ru.mashintsev.data.ExchangeRate;

import java.util.function.Function;

/**
 * Created by i.mashintsev on 17.02.17.
 */
@Data
public class ExchageRateDto {
    private boolean active;
    private String bank;
    private String symbol;
    private double bid;
    private double offer;
    private long updatedAt;

    public static Function<ExchangeRate, ExchageRateDto> map = er -> {
        ExchageRateDto dto = new ExchageRateDto();
        dto.active = er.isActive();
        dto.bank = Bank.valueOf(er.getBank()).label;
        dto.symbol = er.getSymbol();
        dto.bid = er.getBid();
        dto.offer = er.getOffer();
        dto.updatedAt = er.getUpdatedAt().getTime();
        return dto;
    };
}
