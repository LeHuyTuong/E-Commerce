package com.example.ecom.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {
    private Long addressId;
    private String pgPaymentId; // id cong thanh toan
    private String pgStatus;
    private String pgResponseMessage;
    private String pgName;
}
