package com.creditcardadvisor.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> userCards;

    private String preferredCategory; // e.g. BofA 3% choice

    // Getters & Setters
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public List<String> getUserCards() { return userCards; }

    public void setUserCards(List<String> userCards) { this.userCards = userCards; }

    public String getPreferredCategory() { return preferredCategory; }

    public void setPreferredCategory(String preferredCategory) { this.preferredCategory = preferredCategory; }
}
