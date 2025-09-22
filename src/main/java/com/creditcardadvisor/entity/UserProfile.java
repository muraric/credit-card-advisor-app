package com.creditcardadvisor.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String name;

    private String passwordHash; // 🔑 store hashed password, not raw password

    @ElementCollection
    private List<String> userCards;

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public List<String> getUserCards() { return userCards; }
    public void setUserCards(List<String> userCards) { this.userCards = userCards; }
}
