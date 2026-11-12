package com.carvaluation.expert;

import com.carvaluation.auth.CustomUserDetails;
import com.carvaluation.damage.DamageReport;
import com.carvaluation.pricing.ValuationResult;
import com.carvaluation.valuation.PhotoView;
import com.carvaluation.valuation.ValuationSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/expert")
@PreAuthorize("hasAnyRole('EXPERT', 'ADMIN')")
public class ExpertController {

    private final ExpertService expertService;
    private final ObjectMapper objectMapper;

    public ExpertController(ExpertService expertService, ObjectMapper objectMapper) {
        this.expertService = expertService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/dashboard")
    public String expertDashboard(Model model) {
        var pendingSessions = expertService.getPendingReviews();
        model.addAttribute("pendingSessions", pendingSessions);
        return "expert/dashboard";
    }

    @GetMapping("/review/{id}")
    public String reviewPage(@PathVariable("id") Long id, Model model) {
        ValuationSession session = expertService.getSessionById(id)
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
        model.addAttribute("photos", session.getPhotos().stream().map(PhotoView::from).toList());

        return "expert/review";
    }

    @PostMapping("/review/{id}")
    public String submitReview(@PathVariable("id") Long id,
                               @RequestParam("adjustedPrice") Long adjustedPrice,
                               @RequestParam("expertNotes") String expertNotes,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            expertService.submitReview(id, userDetails.getUser(), adjustedPrice, expertNotes);
            redirectAttributes.addFlashAttribute("successMessage", "Đã lưu kết quả thẩm định chuyên gia thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/expert/dashboard";
    }
}
