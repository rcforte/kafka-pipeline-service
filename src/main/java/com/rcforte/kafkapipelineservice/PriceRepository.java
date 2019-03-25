package com.rcforte.kafkapipelineservice;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface PriceRepository extends ReactiveMongoRepository<Price,String> {
}
