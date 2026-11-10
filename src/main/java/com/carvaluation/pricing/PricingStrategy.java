package com.carvaluation.pricing;

import com.carvaluation.damage.DamageReport;
import com.carvaluation.vehicle.Vehicle;

import java.util.Map;

/**
 * Strategy pattern cho phép dễ dàng mở rộng thuật toán định giá
 * (Rule-based Java Engine, ML Model, Hybrid AI Market...)
 */
public interface PricingStrategy {
    ValuationResult calculatePrice(Vehicle vehicle, Integer odoMileage, DamageReport damageReport, Map<String, Object> questionnaire);
}
