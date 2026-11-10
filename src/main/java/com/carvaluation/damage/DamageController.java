package com.carvaluation.damage;

import com.carvaluation.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/damage")
public class DamageController {

    private final DamageAnalysisService damageAnalysisService;

    public DamageController(DamageAnalysisService damageAnalysisService) {
        this.damageAnalysisService = damageAnalysisService;
    }

    /**
     * Phân tích ảnh chụp góc xe để nhận diện xước, móp và hư hỏng ngoại thất
     */
    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<DamageReport>> analyzePhoto(
            @RequestParam(value = "image", required = false) MultipartFile file,
            @RequestParam(value = "viewAngle", defaultValue = "Ngoại thất") String viewAngle,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            byte[] imageBytes;
            String mimeType = "image/jpeg";

            if (file != null && !file.isEmpty()) {
                imageBytes = file.getBytes();
                mimeType = file.getContentType();
            } else if (body != null && body.containsKey("base64Image")) {
                String base64Data = body.get("base64Image");
                if (base64Data.contains(",")) {
                    mimeType = base64Data.substring(5, base64Data.indexOf(";"));
                    base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
                }
                imageBytes = Base64.getDecoder().decode(base64Data);
                if (body.containsKey("viewAngle")) {
                    viewAngle = body.get("viewAngle");
                }
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("Vui lòng gửi ảnh ngoại thất xe"));
            }

            DamageReport report = damageAnalysisService.analyzePhoto(imageBytes, mimeType, viewAngle);
            return ResponseEntity.ok(ApiResponse.ok("Phân tích ngoại thất hoàn tất", report));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Lỗi khi phân tích hư hỏng: " + e.getMessage()));
        }
    }
}
