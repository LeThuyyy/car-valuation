package com.carvaluation.vin;

import com.carvaluation.ai.GeminiVisionService;
import com.carvaluation.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/vin")
public class VinController {

    private final GeminiVisionService visionService;
    private final VinDecoderService decoderService;

    public VinController(GeminiVisionService visionService, VinDecoderService decoderService) {
        this.visionService = visionService;
        this.decoderService = decoderService;
    }

    /**
     * Nhận ảnh chụp số VIN từ camera trình duyệt hoặc file tải lên -> Gemini Vision OCR
     */
    @PostMapping("/scan")
    public ResponseEntity<ApiResponse<VinScanResult>> scanVinImage(
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
                return ResponseEntity.badRequest().body(ApiResponse.error("Vui lòng tải lên file ảnh hoặc chuỗi base64"));
            }

            // 1. OCR số VIN bằng Gemini Vision
            VinScanResult ocrResult = visionService.ocrVinFromImage(imageBytes, mimeType);

            // 2. Nếu đọc được chuỗi VIN, tiến hành giải mã thông tin xe ngay lập tức
            if (ocrResult.getVin() != null && !ocrResult.getVin().isBlank()) {
                VinScanResult decodedResult = decoderService.decodeVin(ocrResult.getVin());
                decodedResult.setSuccess(ocrResult.isSuccess());
                if (!ocrResult.isSuccess()) {
                    decodedResult.setMessage(ocrResult.getMessage());
                }
                return ResponseEntity.ok(ApiResponse.ok("Quét và giải mã VIN thành công", decodedResult));
            }

            return ResponseEntity.ok(ApiResponse.ok("Không nhận diện được VIN rõ ràng", ocrResult));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Lỗi khi xử lý ảnh VIN: " + e.getMessage()));
        }
    }

    /**
     * Giải mã thông tin xe từ chuỗi VIN người dùng nhập thủ công hoặc xác nhận
     */
    @PostMapping("/decode")
    public ResponseEntity<ApiResponse<VinScanResult>> decodeVin(@RequestBody Map<String, String> request) {
        String vin = request.get("vin");
        if (vin == null || vin.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Chuỗi số VIN không được để trống"));
        }

        VinScanResult result = decoderService.decodeVin(vin);
        return ResponseEntity.ok(ApiResponse.ok(result.getMessage(), result));
    }
}
