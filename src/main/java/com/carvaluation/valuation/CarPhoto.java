package com.carvaluation.valuation;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "car_photos")
public class CarPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valuation_session_id", nullable = false)
    private ValuationSession valuationSession;

    @Enumerated(EnumType.STRING)
    @Column(name = "photo_type", nullable = false, length = 30)
    private PhotoType photoType;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "ai_analysis_json", columnDefinition = "TEXT")
    private String aiAnalysisJson;

    @Column(name = "detected_damages_count")
    private Integer detectedDamagesCount = 0;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }

    public CarPhoto() {}

    public CarPhoto(ValuationSession valuationSession, PhotoType photoType, String filePath) {
        this.valuationSession = valuationSession;
        this.photoType = photoType;
        this.filePath = filePath;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ValuationSession getValuationSession() {
        return valuationSession;
    }

    public void setValuationSession(ValuationSession valuationSession) {
        this.valuationSession = valuationSession;
    }

    public PhotoType getPhotoType() {
        return photoType;
    }

    public void setPhotoType(PhotoType photoType) {
        this.photoType = photoType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getAiAnalysisJson() {
        return aiAnalysisJson;
    }

    public void setAiAnalysisJson(String aiAnalysisJson) {
        this.aiAnalysisJson = aiAnalysisJson;
    }

    public Integer getDetectedDamagesCount() {
        return detectedDamagesCount;
    }

    public void setDetectedDamagesCount(Integer detectedDamagesCount) {
        this.detectedDamagesCount = detectedDamagesCount;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
