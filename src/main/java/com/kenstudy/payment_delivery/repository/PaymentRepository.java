package com.kenstudy.payment_delivery.repository;

import com.kenstudy.payment_delivery.model.PaymentDelivery;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface PaymentRepository extends ReactiveCrudRepository<PaymentDelivery, Integer> {
}
