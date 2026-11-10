package com.carvaluation.admin;

import com.carvaluation.auth.Role;
import com.carvaluation.auth.User;
import com.carvaluation.auth.UserRepository;
import com.carvaluation.pricing.ReferenceCarPrice;
import com.carvaluation.pricing.ReferenceCarPriceRepository;
import com.carvaluation.valuation.ValuationSession;
import com.carvaluation.valuation.ValuationSessionRepository;
import com.carvaluation.valuation.ValuationStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ValuationSessionRepository sessionRepository;
    private final ReferenceCarPriceRepository pricingRepository;

    public AdminService(UserRepository userRepository,
                        ValuationSessionRepository sessionRepository,
                        ReferenceCarPriceRepository pricingRepository) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.pricingRepository = pricingRepository;
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalSessions", sessionRepository.count());
        stats.put("pendingExpertReviews", sessionRepository.countByStatus(ValuationStatus.EXPERT_REVIEWING));
        stats.put("completedSessions", sessionRepository.countByStatus(ValuationStatus.COMPLETED));
        stats.put("totalPricingModels", pricingRepository.count());
        return stats;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<ValuationSession> getAllSessions() {
        return sessionRepository.findAll();
    }

    @Transactional
    public void updateUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
        user.setRole(newRole);
        userRepository.save(user);
    }

    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    public List<ReferenceCarPrice> getAllReferencePrices() {
        return pricingRepository.findAll();
    }

    @Transactional
    public ReferenceCarPrice saveReferencePrice(ReferenceCarPrice price) {
        return pricingRepository.save(price);
    }

    @Transactional
    public void deleteReferencePrice(Long id) {
        pricingRepository.deleteById(id);
    }
}
