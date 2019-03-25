package com.rcforte.kafkapipelineservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

@Configuration
public class KafkaTopicsConfig {

  @Value("spring.kafka.bootstrap-servers")
  private String bootstrapServer;

  @Bean
  public ReplyingKafkaTemplate<String, Price, Price> replyingTemplate(
      ProducerFactory<String, Price> pf,
      ConcurrentMessageListenerContainer<String, Price> repliesContainer) {

    return new ReplyingKafkaTemplate<String,Price,Price>(pf, repliesContainer);
  }


  @Bean
  public ConcurrentMessageListenerContainer<String, Price> repliesContainer(
      ConcurrentKafkaListenerContainerFactory<String, Price> containerFactory) {

    ConcurrentMessageListenerContainer<String, Price> repliesContainer =
        containerFactory.createContainer("PricesOutput");
    repliesContainer.getContainerProperties().setGroupId("repliesGroup");
    repliesContainer.setAutoStartup(false);
    return repliesContainer;
  }
}
