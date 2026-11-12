package com.carvaluation.valuation;

import com.carvaluation.auth.User;
import com.carvaluation.vehicle.Vehicle;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "valuation_sessions")
public class ValuationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ValuationStatus status = ValuationStatus.DRAFT;

    @Column(name = "vin_scanned", length = 17)
    private String vinScanned;

    @Column(name = "odo_mileage")
    private Integer odoMileage;

    // Kết quả định giá tiền tệ (VND)
    @Column(name = "estimated_price_vnd")
    private Long estimatedPriceVnd;

    @Column(name = "price_range_min_vnd")
    private Long priceRangeMinVnd;

    @Column(name = "price_range_max_vnd")
    private Long priceRangeMaxVnd;

    @Column(name = "base_market_price_vnd")
    private Long baseMarketPriceVnd;

    @Column(name = "mileage_adjustment_vnd")
    private Long mileageAdjustmentVnd;

    @Column(name = "damage_adjustment_vnd")
    private Long damageAdjustmentVnd;

    @Column(name = "questionnaire_adjustment_vnd")
    private Long questionnaireAdjustmentVnd;

    @Column(name = "questionnaire_json", columnDefinition = "TEXT")
    private String questionnaireJson;

    @Column(name = "ai_report_json", columnDefinition = "TEXT")
    private String aiReportJson;

    // Chuyên gia thẩm định đánh giá & xác nhận
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_id")
    private User assignedExpert;

    @Column(name = "expert_notes", columnDefinition = "TEXT")
    private String expertNotes;

    @Column(name = "expert_adjusted_price_vnd")
    private Long expertAdjustedPriceVnd;

    @Column(name = "expert_reviewed_at")
    private LocalDateTime expertReviewedAt;

    @OneToMany(mappedBy = "valuationSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CarPhoto> photos = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public ValuationSession() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public ValuationStatus getStatus() {
        return status;
    }

    public void setStatus(ValuationStatus status) {
        this.status = status;
    }

    public String getVinScanned() {
        return vinScanned;
    }

    public void setVinScanned(String vinScanned) {
        this.vinScanned = vinScanned;
    }

    public Integer getOdoMileage() {
        return odoMileage;
    }

    public void setOdoMileage(Integer odoMileage) {
        this.odoMileage = odoMileage;
    }

    public Long getDisplayPriceVnd() {
        return expertAdjustedPriceVnd != null ? expertAdjustedPriceVnd : estimatedPriceVnd;
    }

    public Long getEstimatedPriceVnd() {
        return estimatedPriceVnd;
    }

    public void setEstimatedPriceVnd(Long estimatedPriceVnd) {
        this.estimatedPriceVnd = estimatedPriceVnd;
    }

    public Long getPriceRangeMinVnd() {
        return priceRangeMinVnd;
    }

    public void setPriceRangeMinVnd(Long priceRangeMinVnd) {
        this.priceRangeMinVnd = priceRangeMinVnd;
    }

    public Long getPriceRangeMaxVnd() {
        return priceRangeMaxVnd;
    }

    public void setPriceRangeMaxVnd(Long priceRangeMaxVnd) {
        this.priceRangeMaxVnd = priceRangeMaxVnd;
    }

    public Long getBaseMarketPriceVnd() {
        return baseMarketPriceVnd;
    }

    public void setBaseMarketPriceVnd(Long baseMarketPriceVnd) {
        this.baseMarketPriceVnd = baseMarketPriceVnd;
    }

    public Long getMileageAdjustmentVnd() {
        return mileageAdjustmentVnd;
    }

    public void setMileageAdjustmentVnd(Long mileageAdjustmentVnd) {
        this.mileageAdjustmentVnd = mileageAdjustmentVnd;
    }

    public Long getDamageAdjustmentVnd() {
        return damageAdjustmentVnd;
    }

    public void setDamageAdjustmentVnd(Long damageAdjustmentVnd) {
        this.damageAdjustmentVnd = damageAdjustmentVnd;
    }

    public Long getQuestionnaireAdjustmentVnd() {
        return questionnaireAdjustmentVnd;
    }

    public void setQuestionnaireAdjustmentVnd(Long questionnaireAdjustmentVnd) {
        this.questionnaireAdjustmentVnd = questionnaireAdjustmentVnd;
    }

    public String getQuestionnaireJson() {
        return questionnaireJson;
    }

    public void setQuestionnaireJson(String questionnaireJson) {
        this.questionnaireJson = questionnaireJson;
    }

    public String getAiReportJson() {
        return aiReportJson;
    }

    public void setAiReportJson(String aiReportJson) {
        this.aiReportJson = aiReportJson;
    }

    public User getAssignedExpert() {
        return assignedExpert;
    }

    public void setAssignedExpert(User assignedExpert) {
        this.assignedExpert = assignedExpert;
    }

    public String getExpertNotes() {
        return expertNotes;
    }

    public void setExpertNotes(String expertNotes) {
        this.expertNotes = expertNotes;
    }

    public Long getExpertAdjustedPriceVnd() {
        return expertAdjustedPriceVnd;
    }

    public void setExpertAdjustedPriceVnd(Long expertAdjustedPriceVnd) {
        this.expertAdjustedPriceVnd = expertAdjustedPriceVnd;
    }

    public LocalDateTime getExpertReviewedAt() {
        return expertReviewedAt;
    }

    public void setExpertReviewedAt(LocalDateTime expertReviewedAt) {
        this.expertReviewedAt = expertReviewedAt;
    }

    public List<CarPhoto> getPhotos() {
        return photos;
    }

    public void setPhotos(List<CarPhoto> photos) {
        this.photos = photos;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
