package ru.mashintsev.data;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ExchangeRateRxRepository extends ReactiveCrudRepository<ExchangeRate, String> {

    Flux<ExchangeRate> findBySymbolAndActive(Mono<String> symbol, boolean active);

    Flux<ExchangeRate> findByBankAndActive(Mono<String> bank, boolean active);

    Mono<ExchangeRate> findByBankAndSymbol(Mono<String> bank, Mono<String> symbol);

    Flux<ExchangeRate> findByBankAndSymbolAndActive(String bank, String symbol, boolean active);

    Flux<ExchangeRate> findByBankAndSymbol(String bank, String symbol);
}