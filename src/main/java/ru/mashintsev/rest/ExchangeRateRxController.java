package ru.mashintsev.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mashintsev.data.ExchangeRate;
import ru.mashintsev.data.ExchangeRateRxRepository;
import ru.mashintsev.dto.ExchageRateDto;
import ru.mashintsev.dto.SpreadChartDto;
import ru.mashintsev.service.ExchangeRateService;

import java.util.Comparator;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("rates")
public class ExchangeRateRxController {
    private static final Logger log = LogManager.getLogger(ExchangeRateRxController.class);

    private final ExchangeRateRxRepository exchangeRateRxRepository;
    private final ExchangeRateService exchangeRateService;

    @Autowired
    public ExchangeRateRxController(ExchangeRateRxRepository exchangeRateRxRepository, ExchangeRateService exchangeRateService) {
        this.exchangeRateRxRepository = exchangeRateRxRepository;
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping(path = "{symbol}")
    public Flux<ExchageRateDto> getExchangeRates(@PathVariable String symbol) {
        return exchangeRateRxRepository
                .findBySymbolAndActive(Mono.just(symbol), true)
                .map(ExchageRateDto.map)
                .sort(Comparator.comparingDouble(o -> o.getOffer() - o.getBid()));
    }

    @GetMapping(path = "{bank}/{symbol}")
    public Flux<ExchageRateDto> getExchangeRates(@PathVariable String bank, @PathVariable String symbol) {
        return exchangeRateRxRepository
                .findByBankAndSymbol(bank, symbol)
                .map(ExchageRateDto.map)
                .sort(Comparator.comparingDouble(o -> o.getOffer() - o.getBid()));
    }

    @GetMapping(path = "charts/{symbol}/spread")
    public Flux<SpreadChartDto> getSpreadChartData(@PathVariable String symbol) {
        return exchangeRateRxRepository
                .findBySymbolAndActive(Mono.just(symbol), true)
                .map(SpreadChartDto.map)
                .sort(Comparator.comparingDouble(SpreadChartDto::getValue))
                .take(7);
    }

    @GetMapping(path = "charts/{symbol}/ratesHistory")
    public Flux<SpreadChartDto> getRatesHistoryChartData(@PathVariable String symbol) {
//        return exchangeRateRxRepository
//                .findBySymbolAndActive(Mono.just(symbol), true)
//                .groupBy(ExchangeRate::getBank)
//                .concatMap(s -> s.);
        return null;
    }

    @GetMapping(path = "")
    public Flux<ExchageRateDto> getExchangeRates() {
        return exchangeRateRxRepository.findAll().map(ExchageRateDto.map);
    }

    @GetMapping(path = "update")
    public Mono<Void> update() throws ExecutionException, InterruptedException {
        RestTemplate restTemplate = new RestTemplate();
        return Flux.empty()
                   .mergeWith(exchangeRateService.updateGazprombankRates())
                   .mergeWith(exchangeRateService.updateRshbRates())
                   .mergeWith(exchangeRateService.updateBspbRates())
                   .mergeWith(exchangeRateService.updateHomecreditRates())
                   .mergeWith(exchangeRateService.updateRaiffeisenRates())
                   .mergeWith(exchangeRateService.updateOpenRates())
                   .mergeWith(exchangeRateService.updateMkbRates())
                   .mergeWith(exchangeRateService.updateTinkoffRates(restTemplate))
                   .mergeWith(exchangeRateService.updateSberbankRates(restTemplate))
                   .mergeWith(exchangeRateService.updateVtb24Rates(restTemplate))
                   .mergeWith(exchangeRateService.updateRsbRates(restTemplate))
                   .mergeWith(exchangeRateService.updateUnicreditRates(restTemplate))
                   .mergeWith(exchangeRateService.updateAlfabankRates(restTemplate))
                   .parallel()
                   .sequential()
                   .then();
    }
}