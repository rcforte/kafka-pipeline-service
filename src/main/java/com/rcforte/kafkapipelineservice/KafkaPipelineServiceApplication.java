package com.rcforte.kafkapipelineservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KafkaPipelineServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(KafkaPipelineServiceApplication.class, args);
  }
}

