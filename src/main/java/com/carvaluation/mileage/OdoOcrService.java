package com.carvaluation.mileage;

import com.carvaluation.ai.GeminiVisionService;
import org.springframework.stereotype.Service;

@Service
public class OdoOcrService {

    private final GeminiVisionService visionService;

    public OdoOcrService(GeminiVisionService visionService) {
        this.visionService = visionService;
    }

    public OdoScanResult scanOdometer(byte[] imageBytes, String mimeType) {
        if (imageBytes == null || imageBytes.length == 0) {
            return OdoScanResult.failure("Không nhận được dữ liệu ảnh chụp đồng hồ ODO.");
        }
        return visionService.ocrOdoFromImage(imageBytes, mimeType);
    }

    /**
     * Chuẩn hóa dữ liệu ODO người dùng xác nhận hoặc sửa lại
     */
    public int normalizeMileage(Integer inputMileage) {
        if (inputMileage == null || inputMileage < 0) {
            return 0;
        }
        // Giới hạn hợp lý: xe không chạy quá 2,000,000 km
        return Math.min(inputMileage, 2_000_000);
    }
}
