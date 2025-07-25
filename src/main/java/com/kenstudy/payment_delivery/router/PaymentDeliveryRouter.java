package com.kenstudy.payment_delivery.router;

import com.kenstudy.payment_delivery.handler.PaymentDeliverHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class PaymentDeliveryRouter {
    @Bean
    public RouterFunction<ServerResponse> route(PaymentDeliverHandler paymentDeliverHandler) {
        return RouterFunctions.route()
                .path("/v1/payment", builder -> builder
                        .POST("/make-transfer",
                                RequestPredicates.contentType(MediaType.APPLICATION_JSON),
                                paymentDeliverHandler::makePaymentDelivery)

                )
                .build();
    }
}
