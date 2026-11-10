package com.carvaluation.ai;

import com.carvaluation.damage.DamageItem;
import com.carvaluation.damage.DamageReport;
import com.carvaluation.damage.DamageType;
import com.carvaluation.damage.Severity;
import com.carvaluation.mileage.OdoScanResult;
import com.carvaluation.vin.VinScanResult;
import com.carvaluation.vin.VinValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GeminiVisionService {

    private static final Logger log = LoggerFactory.getLogger(GeminiVisionService.class);

    private final GeminiClient geminiClient;
    private final VinValidator vinValidator;
    private final ObjectMapper objectMapper;

    public GeminiVisionService(GeminiClient geminiClient, VinValidator vinValidator, ObjectMapper objectMapper) {
        this.geminiClient = geminiClient;
        this.vinValidator = vinValidator;
        this.objectMapper = objectMapper;
    }

    /**
     * Chức năng 1: OCR số VIN từ ảnh chụp camera
     */
    public VinScanResult ocrVinFromImage(byte[] imageBytes, String mimeType) {
        String prompt = """
            Bạn là hệ thống AI chuyên gia đọc số khung xe ô tô (VIN - Vehicle Identification Number).
            Hãy nhận diện chuỗi ký tự VIN 17 ký tự trong bức ảnh (thường nằm ở góc kính lái, trụ cửa B, hoặc dập trên khung xe).
            Quy tắc tiêu chuẩn VIN:
            - Chính xác 17 ký tự
            - Chỉ gồm các chữ in hoa A-Z và số 0-9
            - KHÔNG bao gồm các chữ cái: I, O, Q

            Hãy trả về kết quả JSON duy nhất theo mẫu:
            {
              "detected": true/false,
              "vin": "CHUỖI_17_KÝ_TỰ",
              "confidence": 0.95,
              "message": "Mô tả ngắn gọn"
            }
            """;

        String jsonText = geminiClient.generateVisionContent(prompt, imageBytes, mimeType);
        VinScanResult result = new VinScanResult();

        try {
            JsonNode root = objectMapper.readTree(cleanJsonMarkdown(jsonText));
            boolean detected = root.path("detected").asBoolean(false);
            String rawVin = root.path("vin").asText("");
            String cleanVin = vinValidator.clean(rawVin);

            result.setVin(cleanVin);
            result.setValid(vinValidator.isValid(cleanVin));
            result.setSuccess(detected && result.isValid());
            result.setMessage(root.path("message").asText(result.isValid() ? "Đã nhận diện số VIN thành công" : "Ảnh mờ hoặc không nhận diện được chuỗi VIN 17 ký tự"));
            result.setNeedsConfirmation(true);
        } catch (Exception e) {
            log.warn("Failed to parse VIN OCR response: {}", e.getMessage());
            result.setSuccess(false);
            result.setMessage("Lỗi xử lý kết quả nhận diện VIN từ AI");
        }

        return result;
    }

    /**
     * Chức năng 2: OCR số km ODO từ ảnh chụp đồng hồ taplo
     */
    public OdoScanResult ocrOdoFromImage(byte[] imageBytes, String mimeType) {
        String prompt = """
            Bạn là hệ thống AI đọc chỉ số công tơ mét đồng hồ ô tô (ODO - Overall Distance Odometer).
            Hãy quan sát kỹ cụm đồng hồ taplo trong ảnh và đọc chỉ số ODO (tổng số km xe đã chạy).
            Lưu ý:
            - Phân biệt ODO với TRIP A, TRIP B, đồng hồ tốc độ (km/h) hoặc đồng hồ vòng tua (RPM).
            - Chỉ lấy phần số nguyên, đơn vị mặc định là km.

            Hãy trả về kết quả JSON duy nhất theo mẫu:
            {
              "detected": true/false,
              "mileage": 52386,
              "unit": "km",
              "confidence": 0.92,
              "needsConfirmation": true,
              "message": "Đã đọc được số ODO 52.386 km từ ảnh"
            }
            """;

        String jsonText = geminiClient.generateVisionContent(prompt, imageBytes, mimeType);
        OdoScanResult result = new OdoScanResult();

        try {
            JsonNode root = objectMapper.readTree(cleanJsonMarkdown(jsonText));
            boolean detected = root.path("detected").asBoolean(false);
            int mileage = root.path("mileage").asInt(0);

            result.setSuccess(detected && mileage > 0);
            result.setMileage(mileage);
            result.setUnit(root.path("unit").asText("km"));
            result.setConfidence(root.path("confidence").asDouble(0.9));
            result.setNeedsConfirmation(true);
            result.setMessage(root.path("message").asText(result.isSuccess() ? "Nhận diện số ODO thành công" : "Không đọc được số ODO từ ảnh"));
        } catch (Exception e) {
            log.warn("Failed to parse ODO OCR response: {}", e.getMessage());
            result.setSuccess(false);
            result.setMessage("Lỗi xử lý kết quả nhận diện ODO từ AI");
        }

        return result;
    }

    /**
     * Chức năng 3: AI nhận diện vết xước, móp và hư hỏng ngoại thất
     */
    public DamageReport analyzeExteriorDamage(byte[] imageBytes, String mimeType, String viewAngle) {
        String prompt = String.format("""
            Bạn là chuyên gia thẩm định xe ô tô chuyên phân tích hình ảnh góc: '%s'.
            Hãy rà soát kỹ các chi tiết bề mặt ngoại thất xe và phát hiện các dấu hiệu hư hỏng nhìn thấy được:
            - SCRATCH: Vết xước sơn (xước bóng, xước vào lớp lót)
            - DENT: Vết móp méo, lõm tôn
            - CRACK: Nứt, vỡ đèn, kính, cản
            - PAINT_CHIP: Tróc sơn, rộp sơn

            Mức độ hư hỏng (severity):
            - LOW: Nhẹ, xước dăm bề mặt, móp nhỏ có thể xử lý nhanh
            - MEDIUM: Rõ ràng, xước sâu hoặc móp trung bình cần sơn/gò lại
            - HIGH: Nghiêm trọng, rách toác, vỡ hỏng lớn cần thay thế
            - UNKNOWN: Khu vực nghi ngờ cần chuyên gia xem xét lại

            LƯU Ý BẮT BUỘC:
            Chỉ nhận diện dấu hiệu nhìn thấy trong ảnh. Tuyệt đối KHÔNG khẳng định xe bị tai nạn nặng, ngập nước hay hỏng kết cấu khung gầm chỉ từ ảnh ngoại thất.

            Trả về JSON duy nhất theo mẫu:
            {
              "detected": true/false,
              "damages": [
                {
                  "type": "SCRATCH",
                  "location": "Vị trí cụ thể (ví dụ: Cánh cửa trước trái)",
                  "severity": "LOW/MEDIUM/HIGH/UNKNOWN",
                  "description": "Mô tả chi tiết vết hư hỏng",
                  "estimatedDeductionVnd": 1500000
                }
              ],
              "overallGrade": "A/B/C/D",
              "needsExpertReview": true/false,
              "disclaimer": "Đánh giá sơ bộ dựa trên ảnh chụp ngoại thất nhìn thấy được."
            }
            """, viewAngle != null ? viewAngle : "Ngoại thất chung");

        String jsonText = geminiClient.generateVisionContent(prompt, imageBytes, mimeType);
        DamageReport report = new DamageReport();
        report.setViewAngle(viewAngle);

        try {
            JsonNode root = objectMapper.readTree(cleanJsonMarkdown(jsonText));
            report.setDetected(root.path("detected").asBoolean(false));
            report.setNeedsExpertReview(root.path("needsExpertReview").asBoolean(false));
            report.setOverallGrade(root.path("overallGrade").asText("B"));
            report.setDisclaimer(root.path("disclaimer").asText("Đánh giá sơ bộ dựa trên ảnh ngoại thất"));

            List<DamageItem> items = new ArrayList<>();
            JsonNode damagesNode = root.path("damages");

            if (damagesNode.isArray()) {
                for (JsonNode itemNode : damagesNode) {
                    DamageItem item = new DamageItem();
                    String typeStr = itemNode.path("type").asText("SCRATCH");
                    try {
                        item.setType(DamageType.valueOf(typeStr.toUpperCase()));
                    } catch (Exception ignored) {
                        item.setType(DamageType.SCRATCH);
                    }

                    String sevStr = itemNode.path("severity").asText("LOW");
                    try {
                        item.setSeverity(Severity.valueOf(sevStr.toUpperCase()));
                    } catch (Exception ignored) {
                        item.setSeverity(Severity.LOW);
                    }

                    item.setLocation(itemNode.path("location").asText("Ngoại thất"));
                    item.setDescription(itemNode.path("description").asText("Dấu hiệu xước/móp nhìn thấy"));
                    item.setEstimatedDeductionVnd(itemNode.path("estimatedDeductionVnd").asLong(1000000L));
                    items.add(item);
                }
            }
            report.setDamages(items);
        } catch (Exception e) {
            log.warn("Failed to parse exterior damage response: {}", e.getMessage());
            report.setDetected(false);
            report.setNeedsExpertReview(true);
            report.setDisclaimer("Không thể phân tích ảnh tự động. Chuyên gia sẽ thẩm định thủ công.");
        }

        return report;
    }

    private String cleanJsonMarkdown(String raw) {
        if (raw == null) return "{}";
        String s = raw.trim();
        if (s.startsWith("```json")) {
            s = s.substring(7);
        } else if (s.startsWith("```")) {
            s = s.substring(3);
        }
        if (s.endsWith("```")) {
            s = s.substring(0, s.length() - 3);
        }
        return s.trim();
    }
}
