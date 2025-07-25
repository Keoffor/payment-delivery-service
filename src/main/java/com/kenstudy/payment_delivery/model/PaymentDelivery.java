package com.kenstudy.payment_delivery.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "payment_delivery")
public class PaymentDelivery {
    @Id
    private Integer id;
    private Double amount;
    @Column("customer_id")
    private Integer customerId;
    @Column("account_id")
    private Integer accountId;
    @Column("transaction_id")
    private Integer transactionId;
    @Column("recipient_id")
    private Integer recipientId;
    private String DeliveryStatus;
    private Boolean status;
    private LocalDate createdDate;
}
