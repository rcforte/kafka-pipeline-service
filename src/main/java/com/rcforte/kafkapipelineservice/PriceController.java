package com.rcforte.kafkapipelineservice;

import lombok.extern.java.Log;
import org.reactivestreams.Publisher;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/prices")
@CrossOrigin("*")
@Log
public class PriceController {
  private static final String REQUEST_TOPIC = "PricesInput";
  private static final String RESPONSE_TOPIC = "PricesOutput";
  private final DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
  private final KafkaTemplate template;
  private final PriceRepository priceRepository;
  private final ReactiveMongoTemplate mongoTemplate;

  public PriceController(KafkaTemplate template, PriceRepository priceRepository, ReactiveMongoTemplate mongoTemplate) {
    this.template = template;
    this.priceRepository = priceRepository;
    this.mongoTemplate = mongoTemplate;
  }

  public Price send(Price price) {
    template.send(REQUEST_TOPIC, price);
    return price;
  }

  @KafkaListener(groupId = "server", topics = RESPONSE_TOPIC)
  public void listen(Price price) {
    log.info("receiving price: " + price);
    priceRepository.save(price).subscribe();
  }

  @Scheduled(fixedRate = 5000)
  public void ticker() throws Exception {
    if(false)
      YahooFinance
        .get("IBM",
            new GregorianCalendar(2017, 11, 31),
            new GregorianCalendar(2018, 11, 31),
            Interval.MONTHLY)
        .getHistory()
        .stream()
        .filter(quote -> quote.getClose() != null)
        .map(quote -> toPrice(quote))
        .map(price -> send(price))
        .collect(Collectors.toList());

    log.info("Generating price");
    Price p = new Price(null, "IBM", dateFormatter.format(new Date()), new Random().nextDouble());
    send(p);
  }

  @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Publisher<Price> prices() {
    return mongoTemplate.tail(new Query(), Price.class).share();
  }

  private Price toPrice(HistoricalQuote quote) {
    return new Price(null, "IBM", dateFormatter.format(quote.getDate().getTime()),
        quote.getClose().doubleValue());
  }
}
