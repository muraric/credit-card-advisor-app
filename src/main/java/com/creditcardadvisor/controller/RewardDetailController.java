package com.creditcardadvisor.controller;

import com.creditcardadvisor.config.PromptLoader;
import com.creditcardadvisor.service.RewardDetailService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class RewardDetailController {

    @Autowired
    private PromptLoader promptLoader;

    @Autowired
    private RewardDetailService rewardDetailService;

    @PostMapping("/cardReward")
    public Map<?, ?> getCardRewards(@org.springframework.web.bind.annotation.RequestBody Map<String, Object> payload) {
        String cardName = (String) payload.get("card_name");
        //return ResponseEntity.ok(responseMap);
        return rewardDetailService.getCardRewards(cardName);
    }
}