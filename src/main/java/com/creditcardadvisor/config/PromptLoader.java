package com.creditcardadvisor.config;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class PromptLoader {
    private final String cardSuggestionPrompt;
    private final String cardRewardPrompt;


    public PromptLoader(ResourceLoader resourceLoader) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:prompts/card-suggestion.txt");
        this.cardSuggestionPrompt = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        Resource resource1 = resourceLoader.getResource("classpath:prompts/card-rewards.txt");
        this.cardRewardPrompt = new String(resource1.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    public String getCardSuggestionPrompt() {
        return cardSuggestionPrompt;
    }

    public String getCardRewardPrompt() {
        return cardRewardPrompt;
    }
}
