package com.creditcardadvisor.repository;

import com.creditcardadvisor.model.SuggestionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuggestionLogRepository extends JpaRepository<SuggestionLog, Long> {
}
