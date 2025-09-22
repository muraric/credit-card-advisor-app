package com.creditcardadvisor.controller;

import com.creditcardadvisor.entity.UserProfile;
import com.creditcardadvisor.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserProfileController {

    @Autowired
    private UserProfileRepository userProfileRepository;

    // Create a profile
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserProfile userProfile) {
        if (userProfileRepository.existsByEmail(userProfile.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "User already exists"));
        }
        return ResponseEntity.ok(userProfileRepository.save(userProfile));
    }

    //Update a profile
    @PutMapping("/{email}")
    public ResponseEntity<?> updateUser(@PathVariable String email, @RequestBody UserProfile updated) {
        return userProfileRepository.findByEmail(email)
                .map(existing -> {
                    existing.setName(updated.getName() != null ? updated.getName() : existing.getName());

                    // ✅ Only update cards if request actually contains cards
                    if (updated.getUserCards() != null && !updated.getUserCards().isEmpty()) {
                        existing.setUserCards(updated.getUserCards());
                    }

                    return ResponseEntity.ok(userProfileRepository.save(existing));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    // Get profile by email
    @GetMapping("/{email}")
    public ResponseEntity<?> getByEmail(@PathVariable String email) {
        return userProfileRepository.findByEmail(email)
                .map(ResponseEntity::ok)
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
