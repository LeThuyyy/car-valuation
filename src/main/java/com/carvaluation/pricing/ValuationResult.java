package com.carvaluation.pricing;

import java.util.ArrayList;
import java.util.List;

public class ValuationResult {

    private Long estimatedPriceVnd; // Giá định giá đề xuất chính xác (VND)
    private Long priceRangeMinVnd; // Khoảng giá thấp nhất (VND)
    private Long priceRangeMaxVnd; // Khoảng giá cao nhất (VND)

    private Long baseMarketPriceVnd; // Giá thị trường cơ sở theo hãng, dòng xe, năm
    private Long mileageAdjustmentVnd; // Điều chỉnh theo số ODO thực tế (âm nếu chạy nhiều, dương nếu đi ít)
    private Long damageAdjustmentVnd; // Khấu trừ hư hỏng ngoại thất (xước, móp)
    private Long questionnaireAdjustmentVnd; // Điều chỉnh theo lịch sử bảo dưỡng, số chủ sở hữu, phụ tùng

    private double aiConfidence = 0.92;
    private String priceGrade; // "GIÁ TỐT", "HỢP LÝ", "GIÁ CAO"
    private List<String> explanationNotes = new ArrayList<>();

    public ValuationResult() {}

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

    public double getAiConfidence() {
        return aiConfidence;
    }

    public void setAiConfidence(double aiConfidence) {
        this.aiConfidence = aiConfidence;
    }

    public String getPriceGrade() {
        return priceGrade;
    }

    public void setPriceGrade(String priceGrade) {
        this.priceGrade = priceGrade;
    }

    public List<String> getExplanationNotes() {
        return explanationNotes;
    }

    public void setExplanationNotes(List<String> explanationNotes) {
        this.explanationNotes = explanationNotes;
    }
}
