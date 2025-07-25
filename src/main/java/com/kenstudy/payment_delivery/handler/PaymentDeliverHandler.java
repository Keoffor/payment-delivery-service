package com.kenstudy.payment_delivery.handler;

import com.kenstudy.payment.PaymentRequestDTO;
import com.kenstudy.payment_delivery.service.PaymentDeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class PaymentDeliverHandler {

    final private PaymentDeliveryService paymentDeliveryService;

    @Autowired
    public PaymentDeliverHandler(PaymentDeliveryService paymentDeliveryService) {
        this.paymentDeliveryService = paymentDeliveryService;
    }

    public Mono<ServerResponse> makePaymentDelivery(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(PaymentRequestDTO.class)
                .flatMap(paymentDeliveryService::makePaymentDelivery)
                .flatMap(transferResponse ->
                        ServerResponse.status(HttpStatus.CREATED)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(transferResponse)
                );
    }
}
