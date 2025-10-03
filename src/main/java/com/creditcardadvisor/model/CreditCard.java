package com.creditcardadvisor.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "credit_card",
        indexes = {
                @Index(name = "idx_card_name", columnList = "cardName"),
                @Index(name = "idx_email_cardname", columnList = "cardName", unique = true)
        }
)
@Getter
@Setter
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String cardName;

    @Column(length = 10000)
    private String rewardDetails;
}
