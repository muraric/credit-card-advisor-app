package com.creditcardadvisor.controller;

import com.creditcardadvisor.repository.UserProfileRepository;
import com.creditcardadvisor.config.PromptLoader;
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
    private UserProfileRepository userProfileRepository;

    @Autowired
    private PromptLoader promptLoader;

    @Value("${openai.api.key}")
    private String openAiKey;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @PostMapping("/get-card-rewards")
    public ResponseEntity<?> getCardRewards(@org.springframework.web.bind.annotation.RequestBody Map<String, Object> payload) {
        try {
            String cardName = (String) payload.get("card_name");

            // Load base system prompt
            String basePrompt = promptLoader.getCardRewardPrompt();

            // Build dynamic user context
            String userPrompt = "The user has these cards: " + String.join(", ", cardName);

            // Build request payload for Responses API
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4.1"); // or gpt-4o
            requestBody.put("tools", List.of(Map.of("type", "web_search_preview"))); // enable web search
            requestBody.put("input", List.of(
                    Map.of("role", "system", "content", basePrompt),
                    Map.of("role", "user", "content", userPrompt)
            ));

            // ✅ Explicitly use okhttp3.RequestBody
            okhttp3.RequestBody body = okhttp3.RequestBody.create(
                    okhttp3.MediaType.parse("application/json"),
                    mapper.writeValueAsString(requestBody)
            );

            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/responses")
                    .header("Authorization", "Bearer " + openAiKey)
                    .header("Content-Type", "application/json")
                    .post(body)
                    .build();

            String responseText;
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return ResponseEntity.status(response.code())
                            .body(Map.of("error", "OpenAI API call failed: " + response.message()));
                }
                String bodyString = response.body().string();

                // Parse full API response into a map
                Map<String, Object> bodyMap = mapper.readValue(bodyString, new TypeReference<Map<String, Object>>() {});

                // The "output" array contains assistant messages
                List<Map<String, Object>> outputs = (List<Map<String, Object>>) bodyMap.get("output");
                if (outputs == null || outputs.isEmpty()) {
                    return ResponseEntity.status(500).body(Map.of("error", "No output from model"));
                }

                // ✅ Scan all outputs for first "output_text"
                responseText = null;
                for (Map<String, Object> outputItem : outputs) {
                    List<Map<String, Object>> contentList = (List<Map<String, Object>>) outputItem.get("content");
                    if (contentList != null) {
                        for (Map<String, Object> contentItem : contentList) {
                            if ("output_text".equals(contentItem.get("type"))) {
                                responseText = (String) contentItem.get("text");
                                break;
                            }
                        }
                    }
                    if (responseText != null) break;
                }

                if (responseText == null) {
                    return ResponseEntity.status(500).body(Map.of("error", "No output_text from model"));
                }
            }

            // ✅ Cleanup: strip markdown fences if present
            responseText = responseText.trim();
            if (responseText.startsWith("```")) {
                responseText = responseText.replaceAll("```(json)?", "").trim();
            }

            System.out.println(responseText);
            // Parse GPT JSON into Map
            Map<String, Object> parsedResponse = mapper.readValue(responseText, new TypeReference<Map<String, Object>>() {});
            Map<String, Object> parsedRewards = (Map<String, Object>) parsedResponse.get("cardReward");

            // Build response
            Map<String, Object> responseMap = Map.of(
                    "cardReward", parsedRewards
            );

            return ResponseEntity.ok(responseMap);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "OpenAI API I/O error: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Error generating suggestions: " + e.getMessage()));
        }
    }
}