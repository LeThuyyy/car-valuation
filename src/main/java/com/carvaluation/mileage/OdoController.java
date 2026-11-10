package com.carvaluation.mileage;

import com.carvaluation.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/odo")
public class OdoController {

    private final OdoOcrService odoOcrService;

    public OdoController(OdoOcrService odoOcrService) {
        this.odoOcrService = odoOcrService;
    }

    /**
     * Nhận ảnh chụp đồng hồ taplo từ camera -> Gemini OCR đọc số km ODO
     */
    @PostMapping("/scan")
    public ResponseEntity<ApiResponse<OdoScanResult>> scanOdometer(
            @RequestParam(value = "image", required = false) MultipartFile file,
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
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("Vui lòng gửi ảnh chụp đồng hồ taplo"));
            }

            OdoScanResult result = odoOcrService.scanOdometer(imageBytes, mimeType);
            return ResponseEntity.ok(ApiResponse.ok(result.getMessage(), result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Lỗi khi đọc chỉ số ODO: " + e.getMessage()));
        }
    }
}
