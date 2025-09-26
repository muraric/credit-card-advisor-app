package com.creditcardadvisor.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class SuggestionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String store;
    private String category;

    @Lob
    private String requestPayload;   // full prompt sent to GPT

    @Lob
    private String responsePayload;  // raw GPT response

    private LocalDateTime createdAt = LocalDateTime.now();

    // optional: field for manual feedback later
    private Boolean isCorrect;       // null = not reviewed, true/false = user feedback
    private String reviewerNote;

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getStore() { return store; }
    public void setStore(String store) { this.store = store; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getRequestPayload() { return requestPayload; }
    public void setRequestPayload(String requestPayload) { this.requestPayload = requestPayload; }

    public String getResponsePayload() { return responsePayload; }
    public void setResponsePayload(String responsePayload) { this.responsePayload = responsePayload; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }

    public String getReviewerNote() { return reviewerNote; }
    public void setReviewerNote(String reviewerNote) { this.reviewerNote = reviewerNote; }
}
