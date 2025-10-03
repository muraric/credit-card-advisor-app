package com.creditcardadvisor.controller;

import com.creditcardadvisor.dto.CreditCardDto;
import com.creditcardadvisor.dto.UserProfileDto;
import com.creditcardadvisor.dto.UserUpdateRequestDto;
import com.creditcardadvisor.model.CreditCard;
import com.creditcardadvisor.model.UserProfile;
import com.creditcardadvisor.repository.CreditCardRepository;
import com.creditcardadvisor.repository.UserProfileRepository;
import com.creditcardadvisor.service.RewardDetailService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors; // if you use .collect(Collectors.toList())


@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserProfileController {

    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private RewardDetailService rewardDetailService;
    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private ObjectMapper objectMapper;
    @PostMapping("/login")
    public ResponseEntity<UserProfile> login(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        UserProfile profile = userProfileRepository.findByEmail(email)
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile();
                    newProfile.setEmail(email);
                    newProfile.setName(""); // optional: user can update later
                    newProfile.setUserCards(new ArrayList<>());
                    return userProfileRepository.save(newProfile);
                });

        return ResponseEntity.ok(profile);
    }

    // Create a profile
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserProfile userProfile) {
        if (userProfileRepository.existsByEmail(userProfile.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "User already exists"));
        }
        return ResponseEntity.ok(userProfileRepository.save(userProfile));
    }

    //Update a profile
 /*   @PutMapping("/{email}")
    public ResponseEntity<?> updateUser(@PathVariable String email, @RequestBody UserProfile updated) {
        return userProfileRepository.findByEmail(email)
                .map(existing -> {
                    existing.setName(updated.getName() != null ? updated.getName() : existing.getName());

                    // ✅ Only update cards if request actually contains cards
                    if (updated.getUserCards() != null && !updated.getUserCards().isEmpty()) {
                        existing.setUserCards(updated.getUserCards());
                    }

                    for (String card : updated.getUserCards()) {
                        // Check if this card already exists for the user
                        boolean exists = creditCardRepository.existsByCardName(card);
                        if (!exists) {
                                          // Persist into credit_card table
                        CreditCard creditCard = new CreditCard();
                        creditCard.setCardName(card);
                            // Call RewardDetailService for each card
                            Map<?, ?> rewardDetails = rewardDetailService.getRewardDetails(card);
                            // Convert to string
                            String rewardStr = rewardDetails != null ? rewardDetails.toString() : "";
                            // Skip persisting if rewardDetails contains "error"
                            if (rewardStr.toLowerCase().contains("error")) {
                                continue;
                            }else {
                                String rewardJson = null;
                                try {
                                    rewardJson = objectMapper.writeValueAsString(rewardDetails);
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                                creditCard.setRewardDetails(rewardJson);
                            }
                             // store JSON/stringified map
                        creditCardRepository.save(creditCard);
                        }
                    }
                    //return ResponseEntity.ok(userProfileRepository.save(existing));
                    userProfileRepository.save(existing);
                    return userProfileRepository.findByEmail(email)
                            .map(user -> {
                                // Build card DTOs
                                List<CreditCardDto> cardDtos = user.getUserCards().stream()
                                        .map(card -> {
                                            String rewardDetails = creditCardRepository
                                                    .findByCardName(card)
                                                    .map(CreditCard::getRewardDetails)
                                                    .orElse("{}");
                                            Object rewardJson;
                                            try {
                                                rewardJson = objectMapper.readValue(rewardDetails, Object.class);
                                            } catch (Exception e) {
                                                System.out.println("Invalid Json");
                                                rewardJson = rewardDetails; // fallback if invalid JSON
                                            }
                                            return new CreditCardDto(card, rewardJson);
                                        }).toList();

                                UserProfileDto dto = new UserProfileDto(
                                        user.getId(),
                                        user.getEmail(),
                                        user.getName(),
                                        user.getPasswordHash(),
                                        cardDtos
                                );

                                return ResponseEntity.ok(dto);
                            })
                            .orElse(ResponseEntity.notFound().build());

                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
*/
    @PutMapping("/{email}")
    public ResponseEntity<UserProfileDto> updateUserCards(
            @PathVariable String email,
            @RequestBody UserUpdateRequestDto request) {

        return userProfileRepository.findByEmail(email)
                .map(user -> {
                    // update user name if provided
                    if (request.getName() != null) {
                        user.setName(request.getName());
                    }

                    // ✅ Clear old cards before replacing
                    user.setUserCards(new ArrayList<>());

                    List<String> newCardList = new ArrayList<>();

                    // process incoming cards
                    for (Map<String, String> cardMap : request.getUserCards()) {
                        String cardName = cardMap.get("card_name");
                        if (cardName == null || cardName.isBlank()) continue;

                        try {
                            boolean exists = creditCardRepository.existsByCardName(cardName);
                            if (!exists) {
                                Map<?, ?> rewardDetails = rewardDetailService.getRewardDetails(cardName);
                                String rewardJson = objectMapper.writeValueAsString(rewardDetails);

                                CreditCard creditCard = new CreditCard();
                                creditCard.setCardName(cardName);
                                creditCard.setRewardDetails(rewardJson);
                                creditCardRepository.save(creditCard);
                            }
                            newCardList.add(cardName);
                        } catch (Exception e) {
                            System.out.println("❌ Failed to serialize reward details for card: " + cardName);
                        }
                    }

                    // ✅ Replace with only the new list
                    user.setUserCards(newCardList);
                    userProfileRepository.save(user);

                    // build response (same as GET)
                    List<CreditCardDto> cardDtos = user.getUserCards().stream()
                            .map(card -> {
                                String rewardStr = creditCardRepository
                                        .findByCardName(card)
                                        .map(CreditCard::getRewardDetails)
                                        .orElse("{}");

                                Object rewardJson;
                                try {
                                    rewardJson = objectMapper.readValue(rewardStr, Object.class);
                                } catch (Exception e) {
                                    rewardJson = new HashMap<>();
                                }

                                return new CreditCardDto(card, rewardJson);
                            }).toList();

                    UserProfileDto dto = new UserProfileDto(
                            user.getId(),
                            user.getEmail(),
                            user.getName(),
                            user.getPasswordHash(),
                            cardDtos
                    );

                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }


    // Get profile by email
    @GetMapping("/{email}")
    public ResponseEntity<UserProfileDto> getUserByEmail(@PathVariable String email) {
        return userProfileRepository.findByEmail(email)
                .map(user -> {
                    // Build card DTOs
                    List<CreditCardDto> cardDtos = user.getUserCards().stream()
                            .map(card -> {
                                String rewardDetails = creditCardRepository
                                        .findByCardName(card)
                                        .map(CreditCard::getRewardDetails)
                                        .orElse("{}");
                                Object rewardJson;
                                try {
                                    rewardJson = objectMapper.readValue(rewardDetails, Object.class);
                                } catch (Exception e) {
                                    System.out.println("Invalid Json");
                                    rewardJson = rewardDetails; // fallback if invalid JSON
                                }
                                return new CreditCardDto(card, rewardJson);
                            }).toList();

                    UserProfileDto dto = new UserProfileDto(
                            user.getId(),
                            user.getEmail(),
                            user.getName(),
                            user.getPasswordHash(),
                            cardDtos
                    );

                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete profile
    @DeleteMapping("/{email}")
    public ResponseEntity<?> deleteByEmail(@PathVariable String email) {
        return userProfileRepository.findByEmail(email).map(profile -> {
            userProfileRepository.delete(profile);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}