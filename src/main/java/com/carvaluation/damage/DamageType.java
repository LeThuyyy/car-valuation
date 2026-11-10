package com.carvaluation.damage;

public enum DamageType {
    SCRATCH("Vết xước sơn"),
    DENT("Vết móp méo"),
    CRACK("Vết nứt vỡ"),
    PAINT_CHIP("Tróc / Rộp sơn"),
    RUST("Dấu hiệu rỉ sét"),
    OTHER("Khác");

    private final String description;

    DamageType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
