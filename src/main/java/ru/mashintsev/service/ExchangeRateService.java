package ru.mashintsev.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mashintsev.data.Bank;
import ru.mashintsev.data.ExchangeRate;
import ru.mashintsev.data.ExchangeRateRxRepository;
import ru.mashintsev.dto.AlfabankRatesDto;
import ru.mashintsev.dto.RsbRatesDto;
import ru.mashintsev.dto.SberbankRateDto;
import ru.mashintsev.dto.TinkoffRatesDto;
import ru.mashintsev.dto.UnicreditRateDto;
import ru.mashintsev.dto.Vtb24RatesDto;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Created by i.mashintsev on 19.02.17.
 */
@Component
public class ExchangeRateService {
    private static final Logger log = LogManager.getLogger(ExchangeRateService.class);

    private final ExchangeRateRxRepository exchangeRateRxRepository;

    public ExchangeRateService(ExchangeRateRxRepository exchangeRateRxRepository) {
        this.exchangeRateRxRepository = exchangeRateRxRepository;
    }

    @Scheduled(cron = "0 0/30 7-22 * * *")
    public void updateRates() {
        log.info("---------------- Start update rates task ------------------");
        RestTemplate restTemplate = new RestTemplate();
        Flux.empty()
            .concatWith(this.updateGazprombankRates())
            .concatWith(this.updateRshbRates())
            .concatWith(this.updateBspbRates())
            .concatWith(this.updateHomecreditRates())
            .concatWith(this.updateRaiffeisenRates())
            .concatWith(this.updateOpenRates())
            .concatWith(this.updateMkbRates())
            .concatWith(this.updateTinkoffRates(restTemplate))
            .concatWith(this.updateSberbankRates(restTemplate))
            .concatWith(this.updateVtb24Rates(restTemplate))
            .concatWith(this.updateRsbRates(restTemplate))
            .concatWith(this.updateUnicreditRates(restTemplate))
            .concatWith(this.updateAlfabankRates(restTemplate))
            .parallel()
            .sequential()
            .then()
            .block();
        log.info("---------------- End update rates task ------------------");
    }

    public Mono<Void> updateGazprombankRates() {
        return Mono.fromCallable(Jsoup.connect("http://www.gazprombank.ru/personal/")::get)
                   .flatMap(doc -> Flux.fromIterable(doc.select(".rates")))
                   .take(1)
                   .flatMap(table -> Flux.fromIterable(table.select("tr")))
                   .map(row -> row.select("td"))
                   .filter(cols -> !cols.isEmpty())
                   .map(cols -> new ExchangeRate(Bank.GAZPROMBANK.name(),
                                                 cols.get(0).text() + "RUB",
                                                 Double.parseDouble(cols.get(1).select("strong").text()),
                                                 Double.parseDouble(cols.get(2).select("strong").text()),
                                                 new Date()))
                   .switchIfEmpty(Flux.error(new Exception("No updates")))
                   .concatWith(Flux.fromIterable(this.toDeactivate(Bank.GAZPROMBANK)))
                   .publish(exchangeRateRxRepository::save)
                   .then()
                   .otherwise(t -> {
                       log.error("Error on " + Bank.GAZPROMBANK.name(), t);
                       return Mono.empty();
                   });
    }

    public Mono<Void> updateRshbRates() {
        return Mono.fromCallable(Jsoup.connect("http://www.rshb.ru/branches/moscow/")::get)
                   .flatMap(doc -> Flux.fromIterable(doc.select("table.b-quotes-table")))
                   .takeLast(1)
                   .flatMap(table -> Flux.fromIterable(table.select("tr")))
                   .map(row -> row.select("td"))
                   .filter(cols -> !cols.isEmpty())
                   .map(cols -> new ExchangeRate(Bank.RSHB.name(),
                                                 cols.get(0).text() + "RUB",
                                                 Double.parseDouble(cols.get(1).text()),
                                                 Double.parseDouble(cols.get(2).text()),
                                                 new Date())
                   )
                   .switchIfEmpty(Flux.error(new Exception("No updates")))
                   .concatWith(Flux.fromIterable(this.toDeactivate(Bank.RSHB)))
                   .publish(exchangeRateRxRepository::save)
                   .then()
                   .otherwise(t -> {
                       log.error("Error on " + Bank.RSHB.name(), t);
                       return Mono.empty();
                   });
    }

    public Mono<Void> updateBspbRates() {
        return Mono.fromCallable(Jsoup.connect("https://i.bspb.ru/currency/rates")::get)
                   .flatMap(doc -> Flux.fromIterable(doc.select("#currency-rates-table")))
                   .flatMap(table -> Flux.fromIterable(table.select("tr")))
                   .map(row -> row.select("td"))
                   .filter(cols -> !cols.isEmpty())
                   .map(cols -> new ExchangeRate(Bank.BSPB.name(),
                                                 cols.get(0).text() + "RUB",
                                                 Double.parseDouble(cols.get(3).text()),
                                                 Double.parseDouble(cols.get(4).text()),
                                                 new Date())
                   )
                   .switchIfEmpty(Flux.error(new Exception("No updates")))
                   .concatWith(Flux.fromIterable(this.toDeactivate(Bank.BSPB)))
                   .publish(exchangeRateRxRepository::save)
                   .then()
                   .otherwise(t -> {
                       log.error("Error on " + Bank.BSPB.name(), t);
                       return Mono.empty();
                   });
    }

    public Mono<Void> updateHomecreditRates() {
        return Mono.fromCallable(Jsoup.connect("http://www.homecredit.ru/")::get)
                   .flatMap(doc -> Flux.fromIterable(doc.select(".currency[rel=3]")))
                   .flatMap(table -> Flux.fromIterable(table.select("tr.s14")))
                   .map(row -> row.select("td"))
                   .filter(cols -> !cols.isEmpty())
                   .map(cols -> new ExchangeRate(Bank.HOMECREDIT.name(),
                                                 cols.get(0).text() + "RUB",
                                                 Double.parseDouble(cols.get(1).text()),
                                                 Double.parseDouble(cols.get(2).text()),
                                                 new Date())
                   )
                   .switchIfEmpty(Flux.error(new Exception("No updates")))
                   .concatWith(Flux.fromIterable(this.toDeactivate(Bank.HOMECREDIT)))
                   .publish(exchangeRateRxRepository::save)
                   .then()
                   .otherwise(t -> {
                       log.error("Error on " + Bank.HOMECREDIT.name(), t);
                       return Mono.empty();
                   });
    }

    public Mono<Void> updateRaiffeisenRates() {
        return Mono.fromCallable(Jsoup.connect("http://www.raiffeisen.ru/currency_rates/#online")::get)
                   .flatMap(doc -> Flux.fromIterable(doc.select(".online_view table.table")))
                   .flatMap(table -> Flux.fromIterable(table.select("tr")))
                   .map(row -> row.select("td"))
                   .filter(cols -> !cols.isEmpty())
                   .map(cols -> new ExchangeRate(Bank.RAIFFEISEN.name(),
                                                 cols.get(0).text() + "RUB",
                                                 Double.parseDouble(cols.get(3).text()),
                                                 Double.parseDouble(cols.get(4).text()),
                                                 new Date())
                   )
                   .switchIfEmpty(Flux.error(new Exception("No updates")))
                   .concatWith(Flux.fromIterable(this.toDeactivate(Bank.RAIFFEISEN)))
                   .publish(exchangeRateRxRepository::save)
                   .then()
                   .otherwise(t -> {
                       log.error("Error on " + Bank.RAIFFEISEN.name(), t);
                       return Mono.empty();
                   });
    }

    public Mono<Void> updateRosbankRates() {
        return Mono.empty();
    }

    public Mono<Void> updateOpenRates() {
        return Mono.fromCallable(Jsoup.connect("https://www.open.ru/#people")::get)
                   .flatMap(doc -> Flux.fromIterable(doc.select(".currency-table")))
                   .take(1)
                   .flatMap(table -> Flux.fromIterable(table.select("tbody tr")))
                   .map(row -> row.select("td"))
                   .filter(cols -> !cols.isEmpty() && NumberUtils.isParsable(cols.get(1).text()))
                   .map(cols -> new ExchangeRate(Bank.OPEN.name(),
                                                 cols.get(0).text() + "RUB",
                                                 Double.parseDouble(cols.get(1).text()),
                                                 Double.parseDouble(cols.get(2).text()),
                                                 new Date())
                   )
                   .switchIfEmpty(Flux.error(new Exception("No updates")))
                   .concatWith(Flux.fromIterable(this.toDeactivate(Bank.OPEN)))
                   .publish(exchangeRateRxRepository::save)
                   .then()
                   .otherwise(t -> {
                       log.error("Error on " + Bank.OPEN.name(), t);
                       return Mono.empty();
                   });
    }

    public Mono<Void> updateMkbRates() {
        return Mono.fromCallable(Jsoup.connect("https://mkb.ru/facility/currency/")::get)
                   .flatMap(doc -> Flux.fromIterable(doc.select(".main_text table")))
                   .skip(1)
                   .take(1)
                   .flatMap(table -> Flux.fromIterable(table.select("tr")))
                   .skip(1)
                   .map(row -> row.select("td"))
                   .filter(cols -> !cols.isEmpty())
                   .map(cols -> new ExchangeRate(Bank.MKB.name(),
                                                 cols.get(1).text() + "RUB",
                                                 Double.parseDouble(cols.get(2).text()),
                                                 Double.parseDouble(cols.get(3).text()),
                                                 new Date())
                   )
                   .switchIfEmpty(Flux.error(new Exception("No updates")))
                   .concatWith(Flux.fromIterable(this.toDeactivate(Bank.MKB)))
                   .publish(exchangeRateRxRepository::save)
                   .then()
                   .otherwise(t -> {
                       log.error("Error on " + Bank.MKB.name(), t);
                       return Mono.empty();
                   });
    }

    public Mono<Void> updateRocketRates() {
        return Mono.fromCallable(Jsoup.connect("https://mkb.ru/facility/currency/")::get)
                   .flatMap(doc -> Flux.fromIterable(doc.select(".rb-rates table")))
                   .skip(1)
                   .take(1)
                   .flatMap(table -> Flux.fromIterable(table.select("tr")))
                   .skip(1)
                   .map(row -> row.select("td"))
                   .filter(cols -> !cols.isEmpty())
                   .map(cols -> new ExchangeRate(Bank.ROCKET.name(),
                                                 cols.get(1).text() + "RUB",
                                                 Double.parseDouble(cols.get(2).text()),
                                                 Double.parseDouble(cols.get(3).text()),
                                                 new Date())
                   )
                   .switchIfEmpty(Flux.error(new Exception("No updates")))
                   .concatWith(Flux.fromIterable(this.toDeactivate(Bank.ROCKET)))
                   .publish(exchangeRateRxRepository::save)
                   .then()
                   .otherwise(t -> {
                       log.error("Error on " + Bank.ROCKET.name(), t);
                       return Mono.empty();
                   });
    }

    public Mono<Void> updateTinkoffRates(RestTemplate restTemplate) {
        if (restTemplate == null)
            restTemplate = new RestTemplate();
        RestTemplate finalRestTemplate = restTemplate;
        return Mono.just("https://api.tinkoff.ru/v1/currency_rates")
                   .map(url -> finalRestTemplate.getForEntity(url, TinkoffRatesDto.class).getBody())
                   .flatMap(dto -> Flux.just(dto.getPayload().getRates()))
                   .filter(rate -> rate.getCategory().equals("SavingAccountTransfers"))
                   .map(rate -> new ExchangeRate(Bank.TINKOFF.name(),
                                                 rate.getFromCurrency().getName() + rate.getToCurrency().getName(),
                                                 rate.getBuy(),
                                                 rate.getSell(),
                                                 new Date()))
                   .switchIfEmpty(Flux.error(new Exception("No updates")))
                   .concatWith(Flux.fromIterable(this.toDeactivate(Bank.TINKOFF)))
                   .publish(exchangeRateRxRepository::save)
                   .then()
                   .otherwise(t -> {
                       log.error("Error on " + Bank.TINKOFF.name(), t);
                       return Mono.empty();
                   });
    }

    public Mono<Void> updateSberbankRates(RestTemplate restTemplate) {
        if (restTemplate == null)
            restTemplate = new RestTemplate();
        RestTemplate finalRestTemplate = restTemplate;
        return Mono.just(URI.create("https://www.sberbank.ru/portalserver/proxy/?pipe=shortCachePipe&url=http%3A%2F%2Flocalhost%2Fsbt-services%2Fservices%2Frest%2FrateService%2Frate%2Fcurrent%3FregionId%3D77%26rateCategory%3Dbeznal%26currencyCode%3D840%26date%3D"))
                   .map(uri -> finalRestTemplate.getForEntity(uri, SberbankRateDto.class).getBody())
                   .map(dto -> dto.getBeznal().get("840").get("0"))
                   .flatMap(rate -> Flux.just(new ExchangeRate(Bank.SBERBANK.name(),
                                                               rate.getIsoCur() + "RUB",
                                                               rate.getBuyValue(),
                                                               rate.getSellValue(),
                                                               new Date())))
                   .concatWith(Mono.just(URI.create("https://www.sberbank.ru/portalserver/proxy/?pipe=shortCachePipe&url=http%3A%2F%2Flocalhost%2Fsbt-services%2Fservices%2Frest%2FrateService%2Frate%2Fcurrent%3FregionId%3D77%26rateCategory%3Dbeznal%26currencyCode%3D978%26date%3D"))
                                   .map(uri -> finalRestTemplate.getForEntity(uri, SberbankRateDto.class).getBody())
                                   .map(dto -> dto.getBeznal().get("978").get("0"))
                                   .flatMap(rate -> Flux.just(new ExchangeRate(Bank.SBERBANK.name(),
                                                                               rate.getIsoCur() + "RUB",
                                                                               rate.getBuyValue(),
                                                                               rate.getSellValue(),
                                                                               new Date()))))
                   .switchIfEmpty(Flux.error(new Exception("No updates")))
                   .concatWith(Flux.fromIterable(this.toDeactivate(Bank.SBERBANK)))
                   .publish(exchangeRateRxRepository::save)
                   .then()
                   .otherwise(t -> {
                       log.error("Error on " + Bank.SBERBANK.name(), t);
                       return Mono.empty();
                   })
                   .then();
    }

    public Mono<Void> updateVtb24Rates(RestTemplate restTemplate) {
        if (restTemplate == null)
            restTemplate = new RestTemplate();
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("{\"action\":\"{\\\"action\\\":\\\"currency\\\"}\",\"scopeData\":\"{\\\"currencyRate\\\":\\\"ExchangePersonal\\\"}\"}", requestHeaders);
        RestTemplate finalRestTemplate = restTemplate;
        return Mono.just("https://www.vtb24.ru/services/ExecuteAction")
                   .map(url -> finalRestTemplate.postForEntity(url, request, Vtb24RatesDto.class).getBody())
                   .flatMap(dto -> Flux.just(dto.getItems()))
                   .filter(rate -> rate.getCurrencyGroupAbbr().equals("tele"))
                   .map(rate -> new ExchangeRate(Bank.VTB24.name(),
                                                 rate.getCurrencyAbbr().length() == 3 ? rate.getCurrencyAbbr() + "RUB" : rate.getCurrencyAbbr().replace("/", ""),
                                                 Double.parseDouble(rate.getBuy().replace(",", ".")),
                                                 Double.parseDouble(rate.getSell().replace(",", ".")),
                                                 new Date()))
                   .switchIfEmpty(Flux.error(new Exception("No updates")))
                   .concatWith(Flux.fromIterable(this.toDeactivate(Bank.VTB24)))
                   .publish(exchangeRateRxRepository::save)
                   .then()
                   .otherwise(t -> {
                       log.error("Error on " + Bank.VTB24.name(), t);
                       return Mono.empty();
                   });
    }

    public Mono<Void> updateRsbRates(RestTemplate restTemplate) {
        if (restTemplate == null)
            restTemplate = new RestTemplate();
        HttpHeaders rsbHeaders = new HttpHeaders();
        rsbHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.put("ajax", Collections.singletonList("1"));
        data.put("table", Collections.singletonList("table_sections"));
        data.put("date", Collections.singletonList(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
        data.put("spacename", Collections.singletonList("sections"));

        HttpEntity<MultiValueMap<String, String>> requestRSB = new HttpEntity<>(data, rsbHeaders);
        final RestTemplate finalRestTemplate = restTemplate;
        return Mono.just("https://www.rsb.ru/bitrix/templates/rsb/ajax/getinnertable.php")
                   .map(url -> finalRestTemplate.postForEntity(url, requestRSB, String.class).getBody())
                   .map(body -> {
                       try {
                           return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(body, RsbRatesDto.class);
                       } catch (IOException e) {
                           throw new RuntimeException(e);
                       }
                   })
                   .flatMap(rate -> Flux.just(new ExchangeRate(Bank.RSB.name(),
                                                               "USDRUB",
                                                               Double.parseDouble(rate.getCourses().getSections_dol_rate().replace(",", ".")),
                                                               Double.parseDouble(rate.getCourses().getSections_dol_sell().replace(",", ".")),
                                                               new Date()),
                                              new ExchangeRate(Bank.RSB.name(),
                                                               "EURRUB",
                                                               Double.parseDouble(rate.getCourses().getSections_eur_rate().replace(",", ".")),
                                                               Double.parseDouble(rate.getCourses().getSections_eur_sell().replace(",", ".")),
                                                               new Date()))
                   )
                   .switchIfEmpty(Flux.error(new Exception("No updates")))
                   .concatWith(Flux.fromIterable(this.toDeactivate(Bank.RSB)))
                   .publish(exchangeRateRxRepository::save)
                   .then()
                   .otherwise(t -> {
                       log.error("Error on " + Bank.RSB.name(), t);
                       return Mono.empty();
                   });
    }

    public Mono<Void> updateUnicreditRates(RestTemplate restTemplate) {
        if (restTemplate == null)
            restTemplate = new RestTemplate();
        HttpHeaders uniHeaders = new HttpHeaders();
        uniHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> uniData = new LinkedMultiValueMap<>();
        uniData.put("date", Collections.singletonList(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)));
        HttpEntity<MultiValueMap<String, String>> requestUnicredit = new HttpEntity<>(uniData, uniHeaders);
        RestTemplate finalRestTemplate = restTemplate;
        return Mono.just("https://www.unicreditbank.ru/post.ucr.getRates.html")
                   .map(url -> finalRestTemplate.postForEntity(url, requestUnicredit, String.class).getBody())
                   .flatMap(body -> {
                       try {
                           return Flux.just(new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(body, UnicreditRateDto[].class));
                       } catch (IOException e) {
                           throw new RuntimeException(e);
                       }
                   })
                   .filter(rate -> rate.getForex() != null)
                   .map(rate -> new ExchangeRate(Bank.UNICREDIT.name(),
                                                 rate.getCurrency1() + rate.getCurrency(),
                                                 rate.getForex().getBuy(),
                                                 rate.getForex().getSell(),
                                                 new Date()))
                   .switchIfEmpty(Flux.error(new Exception("No updates")))
                   .concatWith(Flux.fromIterable(this.toDeactivate(Bank.UNICREDIT)))
                   .publish(exchangeRateRxRepository::save)
                   .then()
                   .otherwise(t -> {
                       log.error("Error on " + Bank.UNICREDIT.name(), t);
                       return Mono.empty();
                   });
    }

    public Mono<Void> updateAlfabankRates(RestTemplate restTemplate) {
        if (restTemplate == null)
            restTemplate = new RestTemplate();
        RestTemplate finalRestTemplate = restTemplate;
        return Mono.just("https://alfabank.ru/ext-json/0.2/exchange/cash")
                   .map(url -> finalRestTemplate.getForEntity(url, AlfabankRatesDto.class).getBody())
                   .flatMap(dto -> Flux.fromIterable(dto.getResponse().getData().entrySet()))
                   .map(entry -> {
                       String symbol = entry.getKey().toUpperCase() + "RUB";
                       AlfabankRatesDto.Rate[] rates = entry.getValue();
                       return new ExchangeRate(Bank.ALFABANK.name(),
                                               symbol,
                                               rates[0].getType().equals("buy") ? rates[0].getValue() : rates[1].getValue(),
                                               rates[0].getType().equals("sell") ? rates[0].getValue() : rates[1].getValue(),
                                               new Date());
                   })
                   .switchIfEmpty(Flux.error(new Exception("No updates")))
                   .concatWith(Flux.fromIterable(this.toDeactivate(Bank.ALFABANK)))
                   .publish(exchangeRateRxRepository::save)
                   .then()
                   .otherwise(t -> {
                       log.error("Error on " + Bank.ALFABANK.name(), t);
                       return Mono.empty();
                   });
    }

    private Iterable<ExchangeRate> toDeactivate(Bank bank) {
        return exchangeRateRxRepository.findByBankAndActive(Mono.just(bank.name()), true)
                                       .doOnNext(exchangeRate -> {
                                           exchangeRate.setActive(false);
                                       })
                                       .toStream()
                                       .collect(Collectors.toList());
    }
}
