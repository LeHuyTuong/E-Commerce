package com.example.ecom.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    private Long orderId;
    private String email;
    private List<OrderItemDTO> orderItems = new ArrayList<>();
    private LocalDate orderDate;
    private PaymentDTO payment;

    @jakarta.validation.constraints.NotNull
    @jakarta.validation.constraints.DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    private String orderStatus;
    private Long addressId;
    private AddressDTO address;
}
