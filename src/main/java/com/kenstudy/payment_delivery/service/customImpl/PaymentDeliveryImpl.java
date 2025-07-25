package com.kenstudy.payment_delivery.service.customImpl;

import com.kenstudy.account.DebitAndCreditResponseDTO;
import com.kenstudy.customer.CustomerAccount;
import com.kenstudy.customer.CustomerResponseDTO;
import com.kenstudy.payment.PaymentRequestDTO;
import com.kenstudy.payment.PaymentResponseDTO;
import com.kenstudy.payment.PaymentStatus;
import com.kenstudy.payment_delivery.config.client.PaymentDeliveryClient;
import com.kenstudy.payment_delivery.exception.PaymentDeliveryFoundException;
import com.kenstudy.payment_delivery.exception.ResourceNotFoundException;
import com.kenstudy.payment_delivery.model.PaymentDelivery;
import com.kenstudy.payment_delivery.repository.PaymentRepository;
import com.kenstudy.payment_delivery.service.PaymentDeliveryService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class PaymentDeliveryImpl implements PaymentDeliveryService {
    private final PaymentDeliveryClient paymentDeliveryClient;
    private final PaymentRepository paymentRepository;
    private final TransactionalOperator txOperator;

    @Autowired
    public PaymentDeliveryImpl(PaymentDeliveryClient paymentDeliveryClient, PaymentRepository paymentRepository,
                               TransactionalOperator txOperator) {
        this.paymentDeliveryClient = paymentDeliveryClient;
        this.paymentRepository = paymentRepository;
        this.txOperator = txOperator;
    }

    @Override
    public Mono<PaymentResponseDTO> makePaymentDelivery(PaymentRequestDTO payDto) {
        if (ObjectUtils.isEmpty(payDto)) {
            return Mono.error(new PaymentDeliveryFoundException("Payment delivery request must not be empty"));
        }
        return Mono.justOrEmpty(payDto.getAccountId())
                .flatMap(paymentDeliveryClient::getCustomerAndAcctDetails)
                .switchIfEmpty(Mono.error(new PaymentDeliveryFoundException("Account not found")))
                .flatMap(acct -> checkCustomerBalance(acct, payDto))
                .flatMap(paymt ->
                        paymentDeliveryClient.getTransactDetails(paymt.getTransactionId())
                                .switchIfEmpty(Mono.error(new PaymentDeliveryFoundException("Transaction not found")))
                                .flatMap(trans -> {
                                    if (!trans.getId().equals(paymt.getTransactionId())
                                            || !trans.getRecipientId().equals(paymt.getRecipientId())) {
                                        Mono.error(new ResourceNotFoundException("Invalid transaction"));
                                    }
                                    return Mono.just(paymt);
                                }))
                .flatMap(payq ->
                        paymentDeliveryClient.transferFund(payq)
                                .switchIfEmpty(Mono.error(new PaymentDeliveryFoundException("Transaction does not exist")))
                                .flatMap(fund -> mapToPayDelivery(payq, fund))
                                .flatMap(paymentRepository::save)
                                .flatMap(this::maptoResponse)
                ).as(txOperator::transactional);
    }

    private Mono<PaymentResponseDTO> maptoResponse(PaymentDelivery saved) {

        return Mono.justOrEmpty(saved)
                .flatMap(delivery -> {
                    PaymentResponseDTO res = new PaymentResponseDTO();
                    res.setId(delivery.getId());
                    res.setCreatedDate(delivery.getCreatedDate());
                    res.setAccountId(delivery.getAccountId());
                    res.setRecipientId(delivery.getRecipientId());
                    res.setAmount(delivery.getAmount());
                    res.setTransactionId(delivery.getTransactionId());
                    res.setDeliveryStatus(delivery.getDeliveryStatus());
                    res.setCustomerId(delivery.getCustomerId());
                    return Mono.just(res);
                });
    }

    private Mono<PaymentDelivery> mapToPayDelivery(PaymentRequestDTO paymt, DebitAndCreditResponseDTO fund) {
        return Mono.justOrEmpty(fund)
                .flatMap(trans -> {
                    PaymentDelivery paymtDelivery = new PaymentDelivery();
                    paymtDelivery.setAccountId(trans.getSenderAccountId());
                    paymtDelivery.setAmount(trans.getAmount());
                    paymtDelivery.setCustomerId(paymt.getCustomerId());
                    paymtDelivery.setRecipientId(trans.getRecipientId());
                    paymtDelivery.setTransactionId(paymt.getTransactionId());
                    paymtDelivery.setDeliveryStatus(PaymentStatus.COMPLETED.name());
                    paymtDelivery.setCreatedDate(LocalDate.now());
                    return Mono.just(paymtDelivery);
                });
    }


    private Mono<PaymentRequestDTO> checkCustomerBalance(CustomerResponseDTO acct, PaymentRequestDTO payDto) {
        return Mono.justOrEmpty(acct)
                .switchIfEmpty(Mono.error(new PaymentDeliveryFoundException("Customer account does not exist")))
                .flatMap(customer -> {
                    if (!customer.getId().equals(payDto.getCustomerId())) {
                        return Mono.error(new PaymentDeliveryFoundException("Account not found with this customer id " + customer.getId()));
                    }

                    if (payDto.getAmount() > 10000) {
                        return Mono.error(new PaymentDeliveryFoundException("Transfer must not exceed $10000 limit per day"));
                    }

                    Optional<CustomerAccount> checkAcctBalance  = customer.getCustomerAccounts().stream()
                            .filter(f -> f.getBalance() - 2 > payDto.getAmount()).findFirst();

                    if (checkAcctBalance.isEmpty()) {
                        return Mono.error(new PaymentDeliveryFoundException("Insufficient balance in account"));
                    }
                    PaymentRequestDTO paymt = new PaymentRequestDTO();
                    paymt.setCustomerId(customer.getId());
                    paymt.setAccountId(customer.getAccountId());
                    paymt.setRecipientId(payDto.getRecipientId());
                    paymt.setAmount(payDto.getAmount());
                    paymt.setTransactionId(payDto.getTransactionId());
                    paymt.setTransactStatus(payDto.getTransactStatus());
                    return Mono.just(paymt);
                });
    }

}
