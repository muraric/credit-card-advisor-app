package com.creditcardadvisor.controller;

import com.creditcardadvisor.entity.UserProfile;
import com.creditcardadvisor.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserProfileController {

    @Autowired
    private UserProfileRepository repo;

    // Create or update a profile
    @PostMapping
    public ResponseEntity<UserProfile> createOrUpdate(@RequestBody UserProfile profile) {
        Optional<UserProfile> existing = repo.findByEmail(profile.getEmail());
        if (existing.isPresent()) {
            UserProfile existingProfile = existing.get();
            existingProfile.setName(profile.getName());
            existingProfile.setUserCards(profile.getUserCards());
            existingProfile.setPreferredCategory(profile.getPreferredCategory());
            return ResponseEntity.ok(repo.save(existingProfile));
        }
        return ResponseEntity.ok(repo.save(profile));
    }

    // Get profile by email
    @GetMapping("/{email}")
    public ResponseEntity<?> getByEmail(@PathVariable String email) {
        return repo.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete profile
    @DeleteMapping("/{email}")
    public ResponseEntity<?> deleteByEmail(@PathVariable String email) {
        return repo.findByEmail(email).map(profile -> {
            repo.delete(profile);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
