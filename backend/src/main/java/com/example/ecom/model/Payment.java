package com.example.ecom.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @OneToOne(mappedBy = "payment", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY)
    private Order order;

    @NotBlank
    @Size(min = 2, message = "Payment method must be at least 2 characters long")
    private String paymentMethod;

    private String pgPaymentId; // id cong thanh toan
    private String pgStatus;
    private String pgResponseMessage;
    private String pgName;

    public Payment(Long paymentId, String pgPaymentId, String pgStatus, String pgResponseMessage, String pgName) {
        this.paymentId = paymentId;
        this.pgPaymentId = pgPaymentId;
        this.pgStatus = pgStatus;
        this.pgResponseMessage = pgResponseMessage;
        this.pgName = pgName;
    }
}
