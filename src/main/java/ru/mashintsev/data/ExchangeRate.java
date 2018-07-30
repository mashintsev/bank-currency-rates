package ru.mashintsev.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@NoArgsConstructor
@Document
@CompoundIndexes({
        @CompoundIndex(name = "bank_symbol", def = "{'bank' : 1, 'symbol': 1}")
})
public class ExchangeRate {
    @Id
    private String id;
    private String bank;
    private String symbol;
    private double bid;
    private double offer;
    @LastModifiedDate
    private Date updatedAt;
    private boolean active;

    public ExchangeRate(String bank, String symbol, double bid, double offer, Date updatedAt) {
        this.bank = bank;
        this.symbol = symbol;
        this.bid = bid;
        this.offer = offer;
        this.updatedAt = updatedAt;
        this.active = true;
    }
}