package com.rcforte.kafkapipelineservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Price {
  private String symbol;
  private String date;
  private Double price;
}
