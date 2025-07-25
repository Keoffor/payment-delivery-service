package com.kenstudy.payment_delivery.config;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClienConfig {
    @Bean
    public WebClient webClient() {

        return WebClient.builder().build();
    }

    @Bean
    public TransactionalOperator transactionalOperator(ReactiveTransactionManager transactionManager) {
        return TransactionalOperator.create(transactionManager);
    }
    @Bean
    public WebProperties.Resources webResources() {
        return new WebProperties.Resources();
    }
}
