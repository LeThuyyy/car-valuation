package com.carvaluation.valuation;

import com.carvaluation.auth.CustomUserDetails;
import com.carvaluation.auth.User;
import com.carvaluation.common.ApiResponse;
import com.carvaluation.damage.DamageReport;
import com.carvaluation.pricing.ValuationResult;
import com.carvaluation.vehicle.Vehicle;
import com.carvaluation.vin.VinDecoderService;
import com.carvaluation.vin.VinScanResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ValuationController {

    private final ValuationService valuationService;
    private final VinDecoderService vinDecoderService;
    private final ObjectMapper objectMapper;

    public ValuationController(ValuationService valuationService,
                               VinDecoderService vinDecoderService,
                               ObjectMapper objectMapper) {
        this.valuationService = valuationService;
        this.vinDecoderService = vinDecoderService;
        this.objectMapper = objectMapper;
    }

    /**
     * Bắt đầu một phiên định giá mới
     */
    @GetMapping("/valuation/start")
    public String startValuation(@AuthenticationPrincipal CustomUserDetails userDetails) {
        ValuationSession session = valuationService.startNewSession(userDetails.getUser());
        return "redirect:/valuation/" + session.getId() + "/vin";
    }

    /**
     * Bước 1: Quét số VIN bằng camera hoặc nhập tay
     */
    @GetMapping("/valuation/{id}/vin")
    public String vinScanStep(@PathVariable("id") Long id,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              Model model) {
        ValuationSession session = valuationService.getSessionForUser(id, userDetails.getUser())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên định giá hoặc bạn không có quyền"));

        model.addAttribute("session", session);
        return "valuation/vin-scan";
    }

    @PostMapping("/valuation/{id}/vin")
    public String handleVinSubmit(@PathVariable("id") Long id,
                                  @RequestParam(value = "vin", required = false) String vin,
                                  @AuthenticationPrincipal CustomUserDetails userDetails) {
        ValuationSession session = valuationService.getSessionForUser(id, userDetails.getUser())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên định giá"));

        if (vin != null && !vin.isBlank()) {
            session.setVinScanned(vin.trim().toUpperCase());
            // Tự động giải mã VIN để điền sẵn vào xe
            VinScanResult decoded = vinDecoderService.decodeVin(vin);
            Vehicle vehicle = session.getVehicle() != null ? session.getVehicle() : new Vehicle();
            vehicle.setUser(userDetails.getUser());
            vehicle.setVin(vin.trim().toUpperCase());
            if (decoded.getMake() != null) vehicle.setMake(decoded.getMake());
            if (decoded.getModel() != null) vehicle.setModel(decoded.getModel());
            if (decoded.getYear() != null) vehicle.setYear(decoded.getYear());
            if (decoded.getTrim() != null) vehicle.setTrim(decoded.getTrim());
            if (decoded.getBodyType() != null) vehicle.setBodyType(decoded.getBodyType());
            if (decoded.getTransmission() != null) vehicle.setTransmission(decoded.getTransmission());
            if (decoded.getFuelType() != null) vehicle.setFuelType(decoded.getFuelType());
            if (decoded.getEngine() != null) vehicle.setEngine(decoded.getEngine());
            if (decoded.getOrigin() != null) vehicle.setOrigin(decoded.getOrigin());

            valuationService.updateVehicleInfo(id, vehicle, userDetails.getUser());
        }

        return "redirect:/valuation/" + id + "/vehicle";
    }

    /**
     * Bước 2: Xác nhận và bổ sung thông tin xe
     */
    @GetMapping("/valuation/{id}/vehicle")
    public String vehicleInfoStep(@PathVariable("id") Long id,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  Model model) {
        ValuationSession session = valuationService.getSessionForUser(id, userDetails.getUser())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên định giá"));

        Vehicle vehicle = session.getVehicle();
        if (vehicle == null) {
            vehicle = new Vehicle();
            vehicle.setYear(2022);
            vehicle.setMake("Toyota");
            vehicle.setModel("Vios");
        }

        model.addAttribute("session", session);
        model.addAttribute("vehicle", vehicle);
        return "valuation/vehicle-info";
    }

    @PostMapping("/valuation/{id}/vehicle")
    public String handleVehicleSubmit(@PathVariable("id") Long id,
                                      @ModelAttribute("vehicle") Vehicle vehicle,
                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        valuationService.updateVehicleInfo(id, vehicle, userDetails.getUser());
        return "redirect:/valuation/" + id + "/odo";
    }

    /**
     * Bước 3: Chụp ảnh đồng hồ ODO / Nhập số km
     */
    @GetMapping("/valuation/{id}/odo")
    public String odoStep(@PathVariable("id") Long id,
                          @AuthenticationPrincipal CustomUserDetails userDetails,
                          Model model) {
        ValuationSession session = valuationService.getSessionForUser(id, userDetails.getUser())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên định giá"));

        model.addAttribute("session", session);
        return "valuation/odo-scan";
    }

    @PostMapping("/valuation/{id}/odo")
    public String handleOdoSubmit(@PathVariable("id") Long id,
                                  @RequestParam(value = "mileage", defaultValue = "35000") Integer mileage,
                                  @AuthenticationPrincipal CustomUserDetails userDetails) {
        valuationService.updateOdo(id, mileage, userDetails.getUser());
        return "redirect:/valuation/" + id + "/photos";
    }

    /**
     * Bước 4: Chụp 4 ảnh chính và AI phân tích xước móp
     */
    @GetMapping("/valuation/{id}/photos")
    public String photosStep(@PathVariable("id") Long id,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             Model model) {
        ValuationSession session = valuationService.getSessionForUser(id, userDetails.getUser())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên định giá"));

        model.addAttribute("session", session);
        model.addAttribute("photos", photoViews(session));
        return "valuation/photos-damage";
    }

    @PostMapping("/valuation/{id}/photos/upload")
    @ResponseBody
    public ResponseEntity<ApiResponse<PhotoView>> handlePhotoUpload(
            @PathVariable("id") Long id,
            @RequestParam("photoType") PhotoType photoType,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            CarPhoto photo = valuationService.uploadAndAnalyzePhoto(id, photoType, file, userDetails.getUser());
            return ResponseEntity.ok(ApiResponse.ok("Tải ảnh và phân tích AI thành công", PhotoView.from(photo)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Lỗi khi tải ảnh: " + e.getMessage()));
        }
    }

    /**
     * Bước 5: Bảng hỏi tình trạng xe (Questionnaire)
     */
    @GetMapping("/valuation/{id}/questionnaire")
    public String questionnaireStep(@PathVariable("id") Long id,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    Model model) {
        ValuationSession session = valuationService.getSessionForUser(id, userDetails.getUser())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên định giá"));

        model.addAttribute("session", session);
        return "valuation/questionnaire";
    }

    @PostMapping("/valuation/{id}/evaluate")
    public String handleEvaluate(@PathVariable("id") Long id,
                                 @RequestParam Map<String, String> formParams,
                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> questionnaire = new HashMap<>(formParams);
        valuationService.processFinalValuation(id, questionnaire, userDetails.getUser());
        return "redirect:/valuation/" + id + "/result";
    }

    /**
     * Bước 6: Báo cáo kết quả định giá chi tiết
     */
    @GetMapping("/valuation/{id}/result")
    public String valuationResult(@PathVariable("id") Long id,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  Model model) {
        ValuationSession session = valuationService.getSessionForUser(id, userDetails.getUser())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hồ sơ định giá"));

        ValuationResult aiResult = null;
        if (session.getAiReportJson() != null && !session.getAiReportJson().isBlank()) {
            try {
                aiResult = objectMapper.readValue(session.getAiReportJson(), ValuationResult.class);
            } catch (Exception ignored) {}
        }

        model.addAttribute("session", session);
        model.addAttribute("vehicle", session.getVehicle());
        model.addAttribute("aiResult", aiResult);
        model.addAttribute("photos", photoViews(session));

        return "valuation/result";
    }

    /**
     * Lịch sử các phiên định giá của người dùng
     */
    @GetMapping("/history")
    public String historyPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        var sessions = valuationService.getUserHistory(userDetails.getUser());
        model.addAttribute("sessions", sessions);
        return "valuation/history";
    }

    /**
     * Xem lại chi tiết một hồ sơ đã lưu
     */
    @GetMapping("/valuation/{id}/detail")
    public String valuationDetail(@PathVariable("id") Long id,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  Model model) {
        return valuationResult(id, userDetails, model);
    }

    private List<PhotoView> photoViews(ValuationSession session) {
        return session.getPhotos().stream().map(PhotoView::from).toList();
    }
}
