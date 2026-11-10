package com.carvaluation.valuation;

public enum PhotoType {
    FRONT("Đầu xe (Góc chính diện)"),
    REAR("Đuôi xe (Góc sau)"),
    LEFT_SIDE("Sườn trái xe"),
    RIGHT_SIDE("Sườn phải xe"),
    CLOSEUP("Vết xước / Móp cận cảnh"),
    VIN("Ảnh chụp số VIN"),
    ODO("Ảnh chụp đồng hồ ODO");

    private final String title;

    PhotoType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
