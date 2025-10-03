package com.creditcardadvisor.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class UserUpdateRequestDto {
    private String name;
    private List<Map<String, String>> userCards; // each contains card_name
}
