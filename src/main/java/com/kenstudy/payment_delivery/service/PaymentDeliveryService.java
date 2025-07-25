package com.kenstudy.payment_delivery.service;

import com.kenstudy.payment.PaymentRequestDTO;
import com.kenstudy.payment.PaymentResponseDTO;
import reactor.core.publisher.Mono;

public interface PaymentDeliveryService {
 Mono<PaymentResponseDTO>makePaymentDelivery(PaymentRequestDTO paymentRequestDTO);
}
