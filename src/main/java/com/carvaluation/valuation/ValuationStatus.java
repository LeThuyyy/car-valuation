package com.carvaluation.valuation;

public enum ValuationStatus {
    DRAFT("Đang tạo hồ sơ"),
    AI_EVALUATED("AI đã định giá"),
    EXPERT_REVIEWING("Chuyên gia đang thẩm định"),
    COMPLETED("Đã hoàn tất thẩm định"),
    CANCELLED("Đã hủy");

    private final String displayName;

    ValuationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
