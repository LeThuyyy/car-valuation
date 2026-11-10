package com.carvaluation.admin;

import com.carvaluation.auth.Role;
import com.carvaluation.pricing.ReferenceCarPrice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", adminService.getDashboardStats());
        model.addAttribute("recentSessions", adminService.getAllSessions());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String usersPage(Model model) {
        model.addAttribute("users", adminService.getAllUsers());
        model.addAttribute("roles", Role.values());
        return "admin/users";
    }

    @PostMapping("/users/{id}/role")
    public String changeRole(@PathVariable("id") Long id,
                             @RequestParam("role") Role role,
                             RedirectAttributes redirectAttributes) {
        try {
            adminService.updateUserRole(id, role);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phân quyền người dùng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle-status")
    public String toggleStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/pricing-data")
    public String pricingDataPage(Model model) {
        model.addAttribute("prices", adminService.getAllReferencePrices());
        model.addAttribute("newPrice", new ReferenceCarPrice());
        return "admin/pricing-data";
    }

    @PostMapping("/pricing-data")
    public String savePricingData(@ModelAttribute("newPrice") ReferenceCarPrice price,
                                  RedirectAttributes redirectAttributes) {
        try {
            adminService.saveReferencePrice(price);
            redirectAttributes.addFlashAttribute("successMessage", "Đã lưu giá xe tham khảo thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/pricing-data";
    }

    @PostMapping("/pricing-data/{id}/delete")
    public String deletePricingData(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteReferencePrice(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa dòng giá xe tham khảo!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/pricing-data";
    }
}
