package com.carvaluation.pricing;

import com.carvaluation.damage.DamageItem;
import com.carvaluation.damage.DamageReport;
import com.carvaluation.vehicle.Vehicle;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class RuleBasedPricingStrategy implements PricingStrategy {

    private final ReferenceCarPriceRepository referenceRepo;

    public RuleBasedPricingStrategy(ReferenceCarPriceRepository referenceRepo) {
        this.referenceRepo = referenceRepo;
    }

    @Override
    public ValuationResult calculatePrice(Vehicle vehicle, Integer odoMileage, DamageReport damageReport, Map<String, Object> questionnaire) {
        ValuationResult result = new ValuationResult();
        List<String> notes = new ArrayList<>();

        int currentYear = Year.now().getValue();
        int carYear = (vehicle.getYear() != null && vehicle.getYear() > 1990) ? vehicle.getYear() : currentYear - 3;
        int age = Math.max(0, currentYear - carYear);

        // 1. Xác định giá thị trường cơ sở (Base Market Price)
        long basePrice = determineBaseMarketPrice(vehicle, age, notes);
        result.setBaseMarketPriceVnd(basePrice);

        // 2. Điều chỉnh theo số ODO thực tế (Mileage Adjustment)
        long mileageAdj = calculateMileageAdjustment(age, odoMileage, basePrice, notes);
        result.setMileageAdjustmentVnd(mileageAdj);

        // 3. Khấu trừ theo tình trạng hư hỏng ngoại thất từ AI (Damage Adjustment)
        long damageAdj = calculateDamageAdjustment(damageReport, notes);
        result.setDamageAdjustmentVnd(damageAdj);

        // 4. Điều chỉnh theo bảng hỏi tình trạng xe (Questionnaire Adjustment)
        long questAdj = calculateQuestionnaireAdjustment(questionnaire, basePrice, notes);
        result.setQuestionnaireAdjustmentVnd(questAdj);

        // 5. Tính giá cuối cùng (Final Estimated Price)
        long finalPrice = basePrice + mileageAdj + damageAdj + questAdj;
        // Đảm bảo giá trị xe không bao giờ xuống dưới 15% giá cơ sở
        finalPrice = Math.max(finalPrice, (long) (basePrice * 0.15));

        // Làm tròn đến hàng triệu đồng
        finalPrice = Math.round(finalPrice / 1_000_000.0) * 1_000_000L;
        result.setEstimatedPriceVnd(finalPrice);

        // Khoảng giá mua bán hợp lý (+/- 3.5%)
        long minPrice = Math.round((finalPrice * 0.965) / 1_000_000.0) * 1_000_000L;
        long maxPrice = Math.round((finalPrice * 1.035) / 1_000_000.0) * 1_000_000L;
        result.setPriceRangeMinVnd(minPrice);
        result.setPriceRangeMaxVnd(maxPrice);

        result.setPriceGrade("MỨC GIÁ HỢP LÝ THỊ TRƯỜNG");
        result.setExplanationNotes(notes);

        return result;
    }

    private long determineBaseMarketPrice(Vehicle vehicle, int age, List<String> notes) {
        String make = vehicle.getMake() != null ? vehicle.getMake().trim() : "Toyota";
        String model = vehicle.getModel() != null ? vehicle.getModel().trim() : "Vios";

        Optional<ReferenceCarPrice> refOpt = referenceRepo.findFirstByMakeIgnoreCaseAndModelIgnoreCaseAndYear(
                make, model, vehicle.getYear());

        if (refOpt.isPresent()) {
            ReferenceCarPrice ref = refOpt.get();
            notes.add(String.format("Giá cơ sở đối chiếu từ dữ liệu xe tham khảo thị trường: %,d VNĐ (%s %s %d)",
                    ref.getBaseUsedPriceVnd(), make, model, vehicle.getYear()));
            return ref.getBaseUsedPriceVnd();
        }

        // Ước lượng tự động dựa trên phân khúc phổ biến tại VN
        long estimatedNewPrice = estimateSegmentNewPrice(make, model);
        double retentionRate = Math.pow(1.0 - 0.085, age); // Khấu hao 8.5%/năm
        long estimatedBase = Math.round(estimatedNewPrice * retentionRate);

        notes.add(String.format("Ước lượng cơ sở theo mô hình khấu hao %d năm tuổi: %,d VNĐ (Mức mới tương đương %,d VNĐ)",
                age, estimatedBase, estimatedNewPrice));

        return estimatedBase;
    }

    private long calculateMileageAdjustment(int age, Integer actualOdo, long basePrice, List<String> notes) {
        if (actualOdo == null || actualOdo <= 0) {
            notes.add("Chưa có số km ODO, áp dụng mức khấu hao tiêu chuẩn.");
            return 0L;
        }

        int expectedMileage = Math.max(10_000, age * 13_500); // Trung bình 13.500 km/năm
        int diff = actualOdo - expectedMileage;

        if (diff > 0) {
            // Xe chạy nhiều hơn bình thường: trừ tiền (khoảng 1.200 đ/km vượt chuẩn)
            long deduction = -Math.round(diff * 1200.0);
            // Giới hạn trừ tối đa 12% giá trị xe
            deduction = Math.max(deduction, -(long)(basePrice * 0.12));
            notes.add(String.format("Số ODO %,d km (vượt mức tiêu chuẩn %,d km cho xe %d năm): giảm trừ %,d VNĐ",
                    actualOdo, expectedMileage, age, Math.abs(deduction)));
            return deduction;
        } else {
            // Xe lướt, chạy ít, giữ gìn: thưởng giá (khoảng 800 đ/km tiết kiệm)
            long bonus = Math.round(Math.abs(diff) * 800.0);
            bonus = Math.min(bonus, (long)(basePrice * 0.06)); // Thưởng tối đa +6%
            notes.add(String.format("Số ODO %,d km (thấp hơn mức tiêu chuẩn %,d km cho xe %d năm - xe đi ít, giữ gìn): cộng thêm %,d VNĐ",
                    actualOdo, expectedMileage, age, bonus));
            return bonus;
        }
    }

    private long calculateDamageAdjustment(DamageReport report, List<String> notes) {
        if (report == null || report.getDamages() == null || report.getDamages().isEmpty()) {
            notes.add("AI không phát hiện dấu hiệu hư hỏng ngoại thất nhìn thấy được.");
            return 0L;
        }

        long totalDeduction = 0L;
        int count = 0;
        for (DamageItem item : report.getDamages()) {
            if (item.getEstimatedDeductionVnd() != null && item.getEstimatedDeductionVnd() > 0) {
                totalDeduction += item.getEstimatedDeductionVnd();
                count++;
            }
        }

        notes.add(String.format("Khấu trừ ngoại thất: AI phát hiện %d dấu hiệu xước/móp, tổng chi phí phục hồi ước tính %,d VNĐ",
                count, totalDeduction));

        return -totalDeduction;
    }

    private long calculateQuestionnaireAdjustment(Map<String, Object> questionnaire, long basePrice, List<String> notes) {
        if (questionnaire == null || questionnaire.isEmpty()) {
            return 0L;
        }

        long adj = 0L;

        // Lịch sử bảo dưỡng chính hãng
        String maintenance = String.valueOf(questionnaire.getOrDefault("maintenance", "REGULAR"));
        if ("FULL_DEALER".equalsIgnoreCase(maintenance)) {
            long bonus = (long) (basePrice * 0.025);
            adj += bonus;
            notes.add(String.format("Lịch sử bảo dưỡng đầy đủ chính hãng (Full Service History): +%,d VNĐ", bonus));
        } else if ("POOR".equalsIgnoreCase(maintenance)) {
            long penalty = -(long) (basePrice * 0.03);
            adj += penalty;
            notes.add(String.format("Không bảo dưỡng định kỳ: %,d VNĐ", penalty));
        }

        // Tình trạng lốp xe
        String tires = String.valueOf(questionnaire.getOrDefault("tires", "GOOD"));
        if ("NEW".equalsIgnoreCase(tires)) {
            adj += 4_000_000L;
            notes.add("Dàn lốp mới thay (date gần, gai lốp dày): +4.000.000 VNĐ");
        } else if ("WORN".equalsIgnoreCase(tires)) {
            adj -= 6_000_000L;
            notes.add("Dàn lốp mòn cần thay thế mới: -6.000.000 VNĐ");
        }

        // Thời hạn đăng kiểm
        String regMonthsStr = String.valueOf(questionnaire.getOrDefault("registrationMonths", "6"));
        try {
            int regMonths = Integer.parseInt(regMonthsStr);
            if (regMonths >= 12) {
                adj += 2_000_000L;
                notes.add("Thời hạn đăng kiểm còn trên 12 tháng: +2.000.000 VNĐ");
            }
        } catch (NumberFormatException ignored) {}

        return adj;
    }

    private long estimateSegmentNewPrice(String make, String model) {
        String m = model.toLowerCase();
        if (m.contains("vios") || m.contains("city") || m.contains("accent") || m.contains("vf 5") || m.contains("soluto")) {
            return 530_000_000L; // Sedan hạng B / A-SUV
        }
        if (m.contains("morning") || m.contains("i10") || m.contains("fadil") || m.contains("vf 3") || m.contains("wigo")) {
            return 380_000_000L; // Xe hạng A
        }
        if (m.contains("cerato") || m.contains("k3") || m.contains("civic") || m.contains("mazda 3") || m.contains("elantra")) {
            return 720_000_000L; // Sedan hạng C
        }
        if (m.contains("cx-5") || m.contains("tucson") || m.contains("cr-v") || m.contains("corolla cross") || m.contains("vf 7")) {
            return 950_000_000L; // C-SUV
        }
        if (m.contains("santa fe") || m.contains("sorento") || m.contains("everest") || m.contains("fortuner") || m.contains("vf 8")) {
            return 1_250_000_000L; // D-SUV 7 chỗ
        }
        if (m.contains("ranger") || m.contains("hilux") || m.contains("d-max") || m.contains("triton")) {
            return 780_000_000L; // Bán tải Pickup
        }
        return 650_000_000L; // Mặc định phổ thông
    }
}
