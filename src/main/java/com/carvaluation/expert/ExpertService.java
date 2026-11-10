package com.carvaluation.expert;

import com.carvaluation.auth.User;
import com.carvaluation.valuation.ValuationSession;
import com.carvaluation.valuation.ValuationSessionRepository;
import com.carvaluation.valuation.ValuationStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ExpertService {

    private final ValuationSessionRepository sessionRepository;

    public ExpertService(ValuationSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public List<ValuationSession> getPendingReviews() {
        return sessionRepository.findByStatusOrderByCreatedAtDesc(ValuationStatus.EXPERT_REVIEWING);
    }

    public List<ValuationSession> getAllReviewedByExpert(Long expertId) {
        return sessionRepository.findByAssignedExpertIdOrderByCreatedAtDesc(expertId);
    }

    public Optional<ValuationSession> getSessionById(Long id) {
        return sessionRepository.findById(id);
    }

    @Transactional
    public ValuationSession submitReview(Long sessionId, User expert, Long adjustedPrice, String expertNotes) {
        ValuationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hồ sơ định giá"));

        session.setAssignedExpert(expert);
        session.setExpertAdjustedPriceVnd(adjustedPrice);
        session.setExpertNotes(expertNotes);
        session.setExpertReviewedAt(LocalDateTime.now());
        session.setStatus(ValuationStatus.COMPLETED);

        return sessionRepository.save(session);
    }
}
