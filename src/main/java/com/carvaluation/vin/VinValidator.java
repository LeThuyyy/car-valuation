package com.carvaluation.vin;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Kiểm tra tính hợp lệ của số VIN theo tiêu chuẩn ISO 3779:
 * - Độ dài chính xác 17 ký tự
 * - Chỉ chứa chữ cái viết hoa và số (A-Z, 0-9)
 * - Loại trừ các chữ cái I, O, Q để tránh nhầm lẫn với 1 và 0
 * - Kiểm tra checksum check digit (ký tự thứ 9) khi áp dụng cho chuẩn Bắc Mỹ / quốc tế
 */
@Component
public class VinValidator {

    private static final Pattern VIN_PATTERN = Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$");

    // Bảng giá trị chuyển đổi cho chữ cái sang số để tính check digit
    private static final int[] VALUES = {
        1, 2, 3, 4, 5, 6, 7, 8, // A, B, C, D, E, F, G, H
        1, 2, 3, 4, 5,          // J, K, L, M, N
        7, 9, 2, 3, 4, 5, 6, 7, 8, 9 // P, R, S, T, U, V, W, X, Y, Z
    };

    // Bảng trọng số các vị trí (1 đến 17)
    private static final int[] WEIGHTS = {
        8, 7, 6, 5, 4, 3, 2, 10, 0, 9, 8, 7, 6, 5, 4, 3, 2
    };

    public boolean isValid(String vin) {
        if (vin == null) return false;
        String cleanVin = clean(vin);
        return VIN_PATTERN.matcher(cleanVin).matches();
    }

    public String clean(String rawVin) {
        if (rawVin == null) return "";
        return rawVin.replaceAll("[\\s\\-\\.]", "").toUpperCase();
    }

    /**
     * Xác thực ký tự thứ 9 (Check Digit) theo thuật toán ISO 3779 / US standard
     * Nếu không khớp check digit nhưng đúng format 17 ký tự, vẫn có thể chấp nhận với cảnh báo
     * vì nhiều nhà sản xuất tại châu Âu hoặc châu Á không bắt buộc check digit.
     */
    public boolean verifyCheckDigit(String vin) {
        if (!isValid(vin)) return false;
        String v = clean(vin);

        int sum = 0;
        for (int i = 0; i < 17; i++) {
            char c = v.charAt(i);
            int value;
            if (Character.isDigit(c)) {
                value = c - '0';
            } else {
                value = getCharValue(c);
            }
            sum += value * WEIGHTS[i];
        }

        int remainder = sum % 11;
        char expectedCheckChar = (remainder == 10) ? 'X' : (char) ('0' + remainder);
        return v.charAt(8) == expectedCheckChar;
    }

    private int getCharValue(char c) {
        if (c >= 'A' && c <= 'H') return VALUES[c - 'A'];
        if (c >= 'J' && c <= 'N') return VALUES[8 + (c - 'J')];
        if (c >= 'P' && c <= 'Z') return VALUES[13 + (c - 'P')];
        return 0;
    }
}
