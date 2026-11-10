package com.carvaluation.damage;

public enum Severity {
    LOW("Dấu hiệu hư hỏng nhẹ"),
    MEDIUM("Hư hỏng nhìn thấy rõ"),
    HIGH("Dấu hiệu hư hỏng nghiêm trọng"),
    UNKNOWN("Không đủ bằng chứng để kết luận");

    private final String meaning;

    Severity(String meaning) {
        this.meaning = meaning;
    }

    public String getMeaning() {
        return meaning;
    }
}
