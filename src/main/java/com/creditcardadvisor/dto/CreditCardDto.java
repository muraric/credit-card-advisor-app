package com.creditcardadvisor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreditCardDto {
    private String card_name;
        private Object rewardDetails;
}
