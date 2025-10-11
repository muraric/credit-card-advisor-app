package com.shomuran.creditcardadvisor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreditCardDto {
    private String issuer;
    private String cardProduct;
    private Object rewardDetails;
}
