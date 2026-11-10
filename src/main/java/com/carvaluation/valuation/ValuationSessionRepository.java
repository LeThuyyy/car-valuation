package com.carvaluation.valuation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ValuationSessionRepository extends JpaRepository<ValuationSession, Long> {
    List<ValuationSession> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<ValuationSession> findByIdAndUserId(Long id, Long userId);
    List<ValuationSession> findByStatusOrderByCreatedAtDesc(ValuationStatus status);
    List<ValuationSession> findByAssignedExpertIdOrderByCreatedAtDesc(Long expertId);
    long countByStatus(ValuationStatus status);
}
