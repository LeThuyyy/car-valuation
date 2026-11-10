package com.carvaluation.pricing;

import com.carvaluation.ai.MarketResearchService;
import com.carvaluation.damage.DamageReport;
import com.carvaluation.vehicle.Vehicle;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PricingService {

    private final PricingStrategy pricingStrategy;
    private final MarketResearchService marketResearchService;

    public PricingService(PricingStrategy pricingStrategy, MarketResearchService marketResearchService) {
        this.pricingStrategy = pricingStrategy;
        this.marketResearchService = marketResearchService;
    }

    public ValuationResult evaluate(Vehicle vehicle, Integer odoMileage, DamageReport damageReport, Map<String, Object> questionnaire) {
        ValuationResult result = pricingStrategy.calculatePrice(vehicle, odoMileage, damageReport, questionnaire);

        // Đối chiếu dữ liệu thị trường trực tuyến từ Gemini AI
        try {
            Map<String, Object> marketData = marketResearchService.researchMarketPrice(
                    vehicle.getMake(), vehicle.getModel(), vehicle.getYear(), vehicle.getTrim());

            if (marketData != null && marketData.containsKey("marketPriceAvg")) {
                long aiAvg = ((Number) marketData.get("marketPriceAvg")).longValue();
                result.getExplanationNotes().add(String.format("Dữ liệu thị trường xe tương đương đối chiếu qua AI: khoảng %,d VNĐ", aiAvg));
            }
        } catch (Exception ignored) {}

        return result;
    }
}
