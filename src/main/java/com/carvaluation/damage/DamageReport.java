package com.carvaluation.damage;

import java.util.ArrayList;
import java.util.List;

public class DamageReport {
    private boolean detected;
    private String viewAngle; // "Đầu xe", "Đuôi xe", "Sườn trái", "Sườn phải", "Cận cảnh"
    private List<DamageItem> damages = new ArrayList<>();
    private String overallGrade = "A"; // A (Xuất sắc), B (Khá), C (Trung bình), D (Kém)
    private boolean needsExpertReview = false;
    private String disclaimer = "Đánh giá sơ bộ dựa trên ảnh chụp ngoại thất nhìn thấy được. Cần thẩm định thực tế để xác nhận kết cấu khung gầm và ngập nước.";

    public DamageReport() {}

    public long calculateTotalDeduction() {
        if (damages == null) return 0L;
        return damages.stream()
                .mapToLong(d -> d.getEstimatedDeductionVnd() != null ? d.getEstimatedDeductionVnd() : 0L)
                .sum();
    }

    public boolean isDetected() {
        return detected;
    }

    public void setDetected(boolean detected) {
        this.detected = detected;
    }

    public String getViewAngle() {
        return viewAngle;
    }

    public void setViewAngle(String viewAngle) {
        this.viewAngle = viewAngle;
    }

    public List<DamageItem> getDamages() {
        return damages;
    }

    public void setDamages(List<DamageItem> damages) {
        this.damages = damages;
    }

    public String getOverallGrade() {
        return overallGrade;
    }

    public void setOverallGrade(String overallGrade) {
        this.overallGrade = overallGrade;
    }

    public boolean isNeedsExpertReview() {
        return needsExpertReview;
    }

    public void setNeedsExpertReview(boolean needsExpertReview) {
        this.needsExpertReview = needsExpertReview;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}
