package com.creditcardadvisor.controller;

import com.creditcardadvisor.dto.StoreInfo;
import com.creditcardadvisor.model.SuggestionLog;
import com.creditcardadvisor.model.UserProfile;
import com.creditcardadvisor.repository.UserProfileRepository;
import com.creditcardadvisor.repository.SuggestionLogRepository;
import com.creditcardadvisor.service.GooglePlacesService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class CardSuggestionController {

    @Autowired
    private GooglePlacesService googlePlacesService;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Value("${openai.api.key}")
    private String openAiKey;

    @Autowired
    private OpenAiService openAiService;

    @Autowired
    private SuggestionLogRepository suggestionLogRepository;

    @PostMapping("/get-card-suggestions")
    public ResponseEntity<?> getCardSuggestions(@RequestBody Map<String, Object> payload) {
        try {
            //List<String> userCards = (List<String>) payload.get("userCards");
            String email = (String) payload.get("email");

            // fetch user cards from DB
            List<String> userCards = userProfileRepository.findByEmail(email)
                    .map(UserProfile::getUserCards)
                    .orElse(new ArrayList<>());
            if (userCards.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No cards found for this user"));
            }
            String store = (String) payload.get("store");
            String category = (String) payload.get("category");
            String currentQuarter = (String) payload.get("currentQuarter");

            // Handle auto-detect (if no store provided)
            if ((store == null || store.isEmpty()) &&
                    payload.containsKey("latitude") && payload.containsKey("longitude")) {

                double latitude = Double.parseDouble(payload.get("latitude").toString());
                double longitude = Double.parseDouble(payload.get("longitude").toString());

                StoreInfo detected = googlePlacesService.detectNearestStore(latitude, longitude);
                if (detected != null) {
                    store = detected.getName();
                    category = detected.getCategory();
                }
            }
            if (store != null && category == null) {
                category = googlePlacesService.getCategoryForStore(store);
            }
            if (store == null || store.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Store name or location required"));
            }

            // GPT prompt
            // GPT prompt with explicit reward rules
            String prompt = "You are a credit card rewards assistant.\n"
                    + "The user has these cards: " + String.join(", ", userCards) + ".\n"
                    + "Store: " + store + ".\n"
                    + "Category: " + category + ".\n"
                    + "Current quarter: " + currentQuarter + ".\n\n"
                    + "Apply the following strict reward rules when calculating the best card:\n"
                    + "- Amex Blue Cash Preferred: 6% groceries, 3% gas/streaming/transit, 1% other.\n"
                    + "- Chase Freedom Flex: 5% on rotating quarterly categories (if category matches), 1% other.\n"
                    + "- Chase Freedom Unlimited: 1.5% everywhere.\n"
                    + "- Chase Freedom (legacy): same rules as Freedom Flex.\n"
                    + "- Citi Double Cash: 2% everywhere.\n"
                    + "- Capital One Venture: 2% everywhere (miles).\n"
                    + "- Bank of America Customized Cash Rewards: 3% in chosen category, 2% groceries, 1% other.\n"
                    + "- Amazon Prime Visa: 5% Amazon/Whole Foods, 2% gas/restaurants/drugstores, 1% other.\n"
                    + "- Costco Anywhere Visa: 4% gas, 3% restaurants/travel, 2% Costco, 1% other.\n\n"
                    + "Rules for decision:\n"
                    + "1. Always prioritize the highest reward percentage among the user's cards.\n"
                    + "2. If two cards tie, prefer the one with the broader earning category.\n"
                    + "3. If no category-specific bonus applies, use the highest flat-rate card.\n\n"
                    + "Output ONLY a valid JSON array in this exact format:\n"
                    + "[{\"card_name\": \"string\", \"expected_reward\": \"string\", \"reasoning\": \"string\"}]";


            System.out.println(prompt);
            ChatMessage message = new ChatMessage("user", prompt);
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model("gpt-3.5-turbo")
                    .messages(List.of(message))
                    .build();

            ChatCompletionResult result = openAiService.createChatCompletion(request);
            String responseText = result.getChoices().get(0).getMessage().getContent();

            // Save log
            SuggestionLog log = new SuggestionLog();
            log.setEmail(email);
            log.setStore(store);
            log.setCategory(category);
            log.setRequestPayload(prompt);
            log.setResponsePayload(responseText);
            suggestionLogRepository.save(log);

            // ✅ Parse GPT JSON into a List before returning
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> parsedSuggestions = mapper.readValue(
                    responseText,
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            // Wrap into a proper response object
            Map<String, Object> responseMap = Map.of(
                    "store", store,
                    "category", category,
                    "suggestions", parsedSuggestions
            );

            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Error generating suggestions: " + e.getMessage()));
        }
    }

    @PutMapping("/suggestions/{id}/review")
    public ResponseEntity<?> reviewSuggestion(
            @PathVariable Long id,
            @RequestBody Map<String, Object> review) {
        return suggestionLogRepository.findById(id)
                .map(log -> {
                    log.setIsCorrect((Boolean) review.get("isCorrect"));
                    log.setReviewerNote((String) review.get("reviewerNote"));
                    suggestionLogRepository.save(log);
                    return ResponseEntity.ok(log);
                })
                .orElse(ResponseEntity.notFound().build());
    }



}
