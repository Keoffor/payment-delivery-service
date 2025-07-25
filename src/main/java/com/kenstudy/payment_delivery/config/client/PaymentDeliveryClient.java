package com.kenstudy.payment_delivery.config.client;

import com.kenstudy.account.DebitAndCreditResponseDTO;
import com.kenstudy.customer.CustomerResponseDTO;
import com.kenstudy.payment.PaymentRequestDTO;
import com.kenstudy.payment_delivery.exception.ErrorMessageResponse;
import com.kenstudy.payment_delivery.exception.PaymentDeliveryFoundException;
import com.kenstudy.payment_delivery.exception.ResourceNotFoundException;
import com.kenstudy.transaction.TransactionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class PaymentDeliveryClient {
    @Autowired
    WebClient webClient;

    String URL_ACCT_HOST = "http://localhost:4001";
    String URL_TRANSACT_HOST= "http://localhost:4002";

    public Mono<CustomerResponseDTO> getCustomerAndAcctDetails(Integer accountId) {
        return webClient.get()
                .uri(URL_ACCT_HOST + "/v1/account/customer-acct-details/{accountId}", accountId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                    clientResponse.bodyToMono(ErrorMessageResponse.class)
                        .flatMap(body -> Mono.error(
                            new PaymentDeliveryFoundException(
                                "Account service client error: " + body.getMessage()))))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                    clientResponse.bodyToMono(ErrorMessageResponse.class)
                        .flatMap(body -> Mono.error(
                            new ResourceNotFoundException(
                                "Account server error: " + body.getMessage()))))
                .bodyToMono(CustomerResponseDTO.class);
    }
    public Mono<DebitAndCreditResponseDTO> transferFund(PaymentRequestDTO payDto) {
        return webClient.post()
                .uri(URL_ACCT_HOST + "/v1/account/fund-transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payDto)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        clientResponse.bodyToMono(ErrorMessageResponse.class)
                                .flatMap(body -> Mono.error(
                                        new PaymentDeliveryFoundException(
                                                "Account service client error: " + body.getMessage()))))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                        clientResponse.bodyToMono(ErrorMessageResponse.class)
                                .flatMap(body -> Mono.error(
                                        new ResourceNotFoundException(
                                                "Account server error: " + body.getMessage()))))
                .bodyToMono(DebitAndCreditResponseDTO.class);
    }

    public Mono<TransactionDto> getTransactDetails(Integer transactId) {
        return webClient.get()
                .uri(URL_TRANSACT_HOST + "/v1/transaction/{transactId}", transactId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                    clientResponse.bodyToMono(ErrorMessageResponse.class)
                        .flatMap(body -> Mono.error(
                            new PaymentDeliveryFoundException(
                                "Transaction service client error: " + body.getMessage()))))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                    clientResponse.bodyToMono(ErrorMessageResponse.class)
                        .flatMap(body -> Mono.error(
                            new ResourceNotFoundException(
                                "Transaction server error: " + body.getMessage()))))
                .bodyToMono(TransactionDto.class);
    }

}
