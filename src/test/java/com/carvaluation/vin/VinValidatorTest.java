package com.carvaluation.vin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VinValidatorTest {

    private VinValidator vinValidator;

    @BeforeEach
    void setUp() {
        vinValidator = new VinValidator();
    }

    @Test
    @DisplayName("Kiểm tra định dạng VIN 17 ký tự hợp lệ")
    void testValidVinFormat() {
        // VIN không chứa các ký tự cấm I, O, Q
        assertTrue(vinValidator.isValid("1HGCR2F83HA123456"));
        assertTrue(vinValidator.isValid("VF8A1XXXXXXXXXXXX".toUpperCase()));
        assertTrue(vinValidator.isValid("KL4CJGSB7HB123456"));
    }

    @Test
    @DisplayName("Kiểm tra VIN chứa ký tự cấm I, O, Q hoặc sai độ dài")
    void testInvalidVinFormat() {
        assertFalse(vinValidator.isValid(null));
        assertFalse(vinValidator.isValid(""));
        assertFalse(vinValidator.isValid("12345")); // Quá ngắn
        assertFalse(vinValidator.isValid("1HGCR2F83HA12345678")); // Quá dài
        assertFalse(vinValidator.isValid("1HGCR2F83HA12345I")); // Chứa chữ I
        assertFalse(vinValidator.isValid("1HGCR2F83HA12345O")); // Chứa chữ O
        assertFalse(vinValidator.isValid("1HGCR2F83HA12345Q")); // Chứa chữ Q
    }

    @Test
    @DisplayName("Kiểm tra làm sạch chuỗi VIN")
    void testCleanVin() {
        assertEquals("1HGCR2F83HA123456", vinValidator.clean("1hg-cr2f8.3ha 123456"));
    }
}
