package com.rcforte.kafkapipelineservice;

import lombok.extern.java.Log;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.Interval;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/prices")
@CrossOrigin("*")
@Log
public class PriceController {
  private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
  private final ReplyingKafkaTemplate<String, Price, Price> template;

  public PriceController(ReplyingKafkaTemplate<String, Price, Price> template) {
    this.template = template;
  }

  public Price sendMessage(Price price) {
    try {
      ProducerRecord<String, Price> record = new ProducerRecord<>("PricesInput", price);
      record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "PricesOutput".getBytes()));
      RequestReplyFuture<String, Price, Price> replyFuture = template.sendAndReceive(record);
      SendResult<String, Price> sendResult = replyFuture.getSendFuture().get();
      log.info("Message sent: " + sendResult.getRecordMetadata());
      ConsumerRecord<String, Price> consumerRecord = replyFuture.get();
      Price received = consumerRecord.value();
      log.info("Message received: " + received);
      return received;
    } catch(Exception e) {
      return null;
    }
  }

  @GetMapping
  public List<Price> getAll() throws Exception {
    Calendar from = new GregorianCalendar(2017, 11, 31);
    Calendar to = new GregorianCalendar(2018, 11, 31);
    Stock stock = YahooFinance.get("IBM", from, to, Interval.MONTHLY);
    List<Price> prices = stock
        .getHistory()
        .stream()
        .filter(quote -> quote.getClose() != null)
        .map(quote -> new Price("IBM", df.format(quote.getDate().getTime()), quote.getClose().doubleValue()))
        .map(price -> sendMessage(price))
        .collect(Collectors.toList());
    return prices;
  }
}
