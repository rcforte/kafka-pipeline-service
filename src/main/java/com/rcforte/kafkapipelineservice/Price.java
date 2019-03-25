package com.rcforte.kafkapipelineservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Document(collection = "Prices")
public class Price {
  
  @Id
  private String id;
  private String symbol;
  private String date;
  private Double price;
}
