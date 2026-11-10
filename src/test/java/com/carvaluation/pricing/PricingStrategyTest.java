package com.carvaluation.pricing;

import com.carvaluation.damage.DamageItem;
import com.carvaluation.damage.DamageReport;
import com.carvaluation.damage.DamageType;
import com.carvaluation.damage.Severity;
import com.carvaluation.vehicle.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class PricingStrategyTest {

    private ReferenceCarPriceRepository priceRepo;
    private RuleBasedPricingStrategy pricingStrategy;

    @BeforeEach
    void setUp() {
        priceRepo = Mockito.mock(ReferenceCarPriceRepository.class);
        pricingStrategy = new RuleBasedPricingStrategy(priceRepo);
    }

    @Test
    @DisplayName("Định giá cơ sở khi có dữ liệu ReferenceCarPrice trong DB")
    void testPricingWithReferenceData() {
        Vehicle vehicle = new Vehicle();
        vehicle.setMake("Toyota");
        vehicle.setModel("Vios");
        vehicle.setYear(2022);

        ReferenceCarPrice ref = new ReferenceCarPrice("Toyota", "Vios", 2022, "1.5G", 580_000_000L, 480_000_000L);
        when(priceRepo.findFirstByMakeIgnoreCaseAndModelIgnoreCaseAndYear(anyString(), anyString(), anyInt()))
                .thenReturn(Optional.of(ref));

        DamageReport damageReport = new DamageReport();
        Map<String, Object> questionnaire = new HashMap<>();
        questionnaire.put("maintenance", "FULL_DEALER"); // +2.5%
        questionnaire.put("tires", "GOOD");
        questionnaire.put("registrationMonths", "6");

        ValuationResult result = pricingStrategy.calculatePrice(vehicle, 27000, damageReport, questionnaire);

        assertNotNull(result);
        assertEquals(480_000_000L, result.getBaseMarketPriceVnd());
        assertTrue(result.getEstimatedPriceVnd() > 0);
        assertTrue(result.getPriceRangeMinVnd() < result.getEstimatedPriceVnd());
        assertTrue(result.getPriceRangeMaxVnd() > result.getEstimatedPriceVnd());
    }

    @Test
    @DisplayName("Khấu trừ chi phí sửa chữa khi AI phát hiện hư hỏng ngoại thất")
    void testDamageDeduction() {
        Vehicle vehicle = new Vehicle();
        vehicle.setMake("VinFast");
        vehicle.setModel("VF8");
        vehicle.setYear(2023);

        DamageReport damageReport = new DamageReport();
        DamageItem scratch = new DamageItem(DamageType.SCRATCH, "Cản trước", Severity.MEDIUM, "Xước cản trước", 1_500_000L);
        DamageItem dent = new DamageItem(DamageType.DENT, "Cửa lái", Severity.LOW, "Móp nhẹ cửa lái", 2_000_000L);
        damageReport.setDamages(List.of(scratch, dent));

        ValuationResult result = pricingStrategy.calculatePrice(vehicle, 15000, damageReport, Map.of());

        assertNotNull(result);
        assertEquals(-3_500_000L, result.getDamageAdjustmentVnd());
    }
}
