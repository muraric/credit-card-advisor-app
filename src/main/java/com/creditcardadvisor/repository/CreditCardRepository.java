package com.creditcardadvisor.repository;

import com.creditcardadvisor.model.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {
    boolean existsByCardName(String cardName);

    Optional<CreditCard> findByCardName(String card);
}
