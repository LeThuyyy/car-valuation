package com.carvaluation.damage;

import com.carvaluation.ai.GeminiVisionService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DamageAnalysisService {

    private final GeminiVisionService visionService;

    public DamageAnalysisService(GeminiVisionService visionService) {
        this.visionService = visionService;
    }

    public DamageReport analyzePhoto(byte[] imageBytes, String mimeType, String viewAngle) {
        return visionService.analyzeExteriorDamage(imageBytes, mimeType, viewAngle);
    }

    /**
     * Tổng hợp kết quả giám định từ tất cả các ảnh ngoại thất của xe
     */
    public DamageReport combineReports(List<DamageReport> reports) {
        DamageReport consolidated = new DamageReport();
        consolidated.setViewAngle("Tổng hợp toàn bộ ngoại thất");

        List<DamageItem> allDamages = new ArrayList<>();
        boolean anyDetected = false;
        boolean anyNeedsExpert = false;

        for (DamageReport r : reports) {
            if (r.isDetected()) anyDetected = true;
            if (r.isNeedsExpertReview()) anyNeedsExpert = true;
            if (r.getDamages() != null) {
                allDamages.addAll(r.getDamages());
            }
        }

        consolidated.setDetected(anyDetected);
        consolidated.setNeedsExpertReview(anyNeedsExpert || allDamages.size() > 3);
        consolidated.setDamages(allDamages);

        // Đánh giá xếp hạng ngoại thất tổng thể
        int damageCount = allDamages.size();
        if (damageCount == 0) {
            consolidated.setOverallGrade("A+ (Gần như mới)");
        } else if (damageCount <= 2) {
            consolidated.setOverallGrade("A (Xuất sắc, xước dăm tối thiểu)");
        } else if (damageCount <= 4) {
            consolidated.setOverallGrade("B (Khá, có xước/móp nhỏ phổ biến)");
        } else if (damageCount <= 7) {
            consolidated.setOverallGrade("C (Trung bình, cần tút tát sơn gò)");
        } else {
            consolidated.setOverallGrade("D (Nhiều hư hỏng, cần phục hồi lớn)");
        }

        return consolidated;
    }
}
