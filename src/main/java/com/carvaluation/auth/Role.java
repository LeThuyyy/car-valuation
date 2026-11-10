package com.carvaluation.auth;

public enum Role {
    USER("Khách hàng"),
    EXPERT("Chuyên gia thẩm định"),
    ADMIN("Quản trị viên");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
