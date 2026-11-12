package com.carvaluation.valuation;

import com.carvaluation.admin.AdminController;
import com.carvaluation.admin.AdminService;
import com.carvaluation.ai.GeminiVisionService;
import com.carvaluation.auth.CustomUserDetails;
import com.carvaluation.auth.Role;
import com.carvaluation.auth.SecurityConfig;
import com.carvaluation.auth.User;
import com.carvaluation.expert.ExpertController;
import com.carvaluation.expert.ExpertService;
import com.carvaluation.mileage.OdoController;
import com.carvaluation.mileage.OdoOcrService;
import com.carvaluation.mileage.OdoScanResult;
import com.carvaluation.vehicle.Vehicle;
import com.carvaluation.vin.VinController;
import com.carvaluation.vin.VinDecoderService;
import com.carvaluation.vin.VinScanResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {ValuationController.class, VinController.class, OdoController.class,
        ExpertController.class, AdminController.class})
@Import(SecurityConfig.class)
class WizardContractTest {
    @Autowired MockMvc mvc;
    @MockBean ValuationService valuationService;
    @MockBean VinDecoderService vinDecoderService;
    @MockBean GeminiVisionService visionService;
    @MockBean OdoOcrService odoOcrService;
    @MockBean ExpertService expertService;
    @MockBean AdminService adminService;

    private CustomUserDetails signedIn(long id, Role role) {
        User person = new User();
        person.setId(id);
        person.setUsername("viewer" + id);
        person.setPassword("test-hash");
        person.setRole(role);
        return new CustomUserDetails(person);
    }

    private ValuationSession session(User owner, ValuationStatus status) {
        ValuationSession session = new ValuationSession();
        session.setId(7L);
        session.setUser(owner);
        session.setStatus(status);
        session.setCreatedAt(LocalDateTime.of(2026, 11, 12, 10, 0));
        Vehicle vehicle = new Vehicle();
        vehicle.setMake("Toyota");
        vehicle.setModel("Vios");
        vehicle.setYear(2022);
        session.setVehicle(vehicle);
        session.setEstimatedPriceVnd(450_000_000L);
        session.setPriceRangeMinVnd(430_000_000L);
        session.setPriceRangeMaxVnd(470_000_000L);
        return session;
    }

    @Test
    void wizardPagesRenderCameraScriptsAndExpectedJsonContract() throws Exception {
        CustomUserDetails owner = signedIn(1, Role.USER);
        ValuationSession session = session(owner.getUser(), ValuationStatus.DRAFT);
        when(valuationService.getSessionForUser(7L, owner.getUser())).thenReturn(Optional.of(session));

        mvc.perform(get("/valuation/7/vin").with(user(owner)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("scanVinImage(base64Data)")))
                .andExpect(content().string(containsString("base64Image: base64Data")))
                .andExpect(content().string(containsString("fetch(\"/api/vin/decode\"")))
                .andExpect(content().string(containsString("headers[csrfHeader] = csrfToken")));
        mvc.perform(get("/valuation/7/odo").with(user(owner)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("scanOdoImage(base64Data)")))
                .andExpect(content().string(containsString("base64Image: base64Data")));
    }

    @Test
    void apiRejectsPostWithoutCsrf() throws Exception {
        mvc.perform(post("/api/vin/scan").with(user(signedIn(1, Role.USER)))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        verify(visionService, never()).ocrVinFromImage(any(), any());
    }

    @Test
    void scanAndDecodeEndpointsAcceptBrowserPayload() throws Exception {
        CustomUserDetails owner = signedIn(1, Role.USER);
        VinScanResult vin = new VinScanResult();
        vin.setVin("1HGCM82633A004352");
        vin.setValid(true);
        vin.setSuccess(true);
        when(visionService.ocrVinFromImage(any(), eq("image/png"))).thenReturn(vin);
        when(vinDecoderService.decodeVin(vin.getVin())).thenReturn(vin);
        when(odoOcrService.scanOdometer(any(), eq("image/png")))
                .thenReturn(new OdoScanResult(true, 0, "km", true, "OK"));
        String image = "{\"base64Image\":\"data:image/png;base64,AQID\"}";

        mvc.perform(post("/api/vin/scan").with(user(owner)).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(image))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.valid").value(true));
        mvc.perform(post("/api/odo/scan").with(user(owner)).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(image))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.mileage").value(0));
        mvc.perform(post("/api/vin/decode").with(user(owner)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vin\":\"1HGCM82633A004352\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.vin").value(vin.getVin()));
    }

    @Test
    void invalidVinAndMileageDoNotAdvanceWizard() throws Exception {
        CustomUserDetails owner = signedIn(1, Role.USER);
        when(valuationService.getSessionForUser(7L, owner.getUser()))
                .thenReturn(Optional.of(session(owner.getUser(), ValuationStatus.DRAFT)));
        when(vinDecoderService.decodeVin("BAD")).thenReturn(VinScanResult.failure("BAD", "VIN không hợp lệ"));

        mvc.perform(post("/valuation/7/vin").with(user(owner)).with(csrf()).param("vin", "BAD"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/valuation/7/vin"))
                .andExpect(flash().attributeExists("errorMessage"));
        mvc.perform(post("/valuation/7/odo").with(user(owner)).with(csrf()).param("mileage", "-1"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/valuation/7/odo"));
        mvc.perform(post("/valuation/7/odo").with(user(owner)).with(csrf()).param("mileage", "1000001"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/valuation/7/odo"));
        verify(valuationService, never()).updateOdo(any(), any(), any());
        verify(valuationService, never()).updateVinAndVehicleInfo(any(), any(), any(), any());
    }

    @Test
    void validVinAndMileageAdvanceWizard() throws Exception {
        CustomUserDetails owner = signedIn(1, Role.USER);
        when(valuationService.getSessionForUser(7L, owner.getUser()))
                .thenReturn(Optional.of(session(owner.getUser(), ValuationStatus.DRAFT)));
        VinScanResult vin = new VinScanResult();
        vin.setVin("1HGCM82633A004352");
        vin.setValid(true);
        when(vinDecoderService.decodeVin(" 1hgcm82633a004352 ")).thenReturn(vin);

        mvc.perform(post("/valuation/7/vin").with(user(owner)).with(csrf())
                        .param("vin", " 1hgcm82633a004352 "))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/valuation/7/vehicle"));
        verify(valuationService).updateVinAndVehicleInfo(eq(7L), eq(vin.getVin()), any(), eq(owner.getUser()));
        mvc.perform(post("/valuation/7/odo").with(user(owner)).with(csrf()).param("mileage", "0"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/valuation/7/photos"));
        verify(valuationService).updateOdo(7L, 0, owner.getUser());
    }

    @Test
    void draftWizardPagesRenderWithRealSessionData() throws Exception {
        CustomUserDetails owner = signedIn(1, Role.USER);
        ValuationSession session = session(owner.getUser(), ValuationStatus.DRAFT);
        when(valuationService.getSessionForUser(7L, owner.getUser())).thenReturn(Optional.of(session));
        mvc.perform(get("/valuation/7/vehicle").with(user(owner))).andExpect(status().isOk())
                .andExpect(content().string(containsString("/valuation/7/vehicle")));
        mvc.perform(get("/valuation/7/photos").with(user(owner))).andExpect(status().isOk())
                .andExpect(content().string(containsString("/valuation/7/questionnaire")))
                .andExpect(content().string(containsString("const sessionId = 7;")));
        mvc.perform(get("/valuation/7/questionnaire").with(user(owner))).andExpect(status().isOk())
                .andExpect(content().string(containsString("/valuation/7/evaluate")));
    }

    @Test
    void exteriorPhotoSlotsUseAcceptedTypesAndUploadBothSides() throws Exception {
        CustomUserDetails owner = signedIn(1, Role.USER);
        ValuationSession valuation = session(owner.getUser(), ValuationStatus.DRAFT);
        when(valuationService.getSessionForUser(7L, owner.getUser())).thenReturn(Optional.of(valuation));

        var page = mvc.perform(get("/valuation/7/photos").with(user(owner)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        for (PhotoType type : List.of(PhotoType.FRONT, PhotoType.REAR,
                PhotoType.LEFT_SIDE, PhotoType.RIGHT_SIDE, PhotoType.CLOSEUP)) {
            org.junit.jupiter.api.Assertions.assertTrue(page.contains("data-type=\"" + type.name() + "\""),
                    "Photo slot does not match server enum: " + type);
        }
        for (PhotoType type : List.of(PhotoType.LEFT_SIDE, PhotoType.RIGHT_SIDE)) {
            CarPhoto saved = new CarPhoto(valuation, type, "cars/test.jpg");
            saved.setId(type == PhotoType.LEFT_SIDE ? 11L : 12L);
            when(valuationService.uploadAndAnalyzePhoto(eq(7L), eq(type), any(), eq(owner.getUser())))
                    .thenReturn(saved);
            mvc.perform(multipart("/valuation/7/photos/upload")
                            .file(new MockMultipartFile("file", "side.jpg", "image/jpeg", new byte[]{1, 2, 3}))
                            .param("photoType", type.name())
                            .with(user(owner)).with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.photoType").value(type.name()));
            verify(valuationService).uploadAndAnalyzePhoto(eq(7L), eq(type), any(), eq(owner.getUser()));
        }
    }

    @Test
    void completedReportUsesExpertPriceAndReviewerFields() throws Exception {
        CustomUserDetails owner = signedIn(1, Role.USER);
        CustomUserDetails expert = signedIn(2, Role.EXPERT);
        ValuationSession session = session(owner.getUser(), ValuationStatus.COMPLETED);
        session.setAssignedExpert(expert.getUser());
        session.setExpertAdjustedPriceVnd(475_000_000L);
        session.setExpertNotes("Đã kiểm tra xe thực tế");
        when(valuationService.getSessionForViewer(7L, owner.getUser())).thenReturn(Optional.of(session));
        mvc.perform(get("/valuation/7/result").with(user(owner)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("viewer2")))
                .andExpect(content().string(containsString("475,000,000")));
    }

    @Test
    void missingReportIsNotExposedToAnotherCustomer() throws Exception {
        CustomUserDetails stranger = signedIn(9, Role.USER);
        when(valuationService.getSessionForViewer(7L, stranger.getUser())).thenReturn(Optional.empty());
        mvc.perform(get("/valuation/7/detail").with(user(stranger))).andExpect(status().isNotFound());
    }

    @Test
    void reportHistoryAndStaffPagesRenderWithPopulatedSessions() throws Exception {
        CustomUserDetails owner = signedIn(1, Role.USER);
        CustomUserDetails expert = signedIn(2, Role.EXPERT);
        CustomUserDetails admin = signedIn(3, Role.ADMIN);
        ValuationSession session = session(owner.getUser(), ValuationStatus.EXPERT_REVIEWING);
        when(valuationService.getSessionForViewer(7L, owner.getUser())).thenReturn(Optional.of(session));
        when(valuationService.getSessionForViewer(7L, admin.getUser())).thenReturn(Optional.of(session));
        when(valuationService.getSessionForViewer(7L, expert.getUser())).thenReturn(Optional.of(session));
        when(valuationService.getUserHistory(owner.getUser())).thenReturn(List.of(session));
        when(expertService.getPendingReviews()).thenReturn(List.of(session));
        when(expertService.getSessionById(7L)).thenReturn(Optional.of(session));
        when(adminService.getAllSessions()).thenReturn(List.of(session));
        when(adminService.getDashboardStats()).thenReturn(Map.of("totalUsers", 3, "totalSessions", 1,
                "pendingExpertReviews", 1, "totalPricingModels", 1));

        mvc.perform(get("/valuation/7/result").with(user(owner))).andExpect(status().isOk());
        mvc.perform(get("/history").with(user(owner))).andExpect(status().isOk())
                .andExpect(content().string(containsString("CHỜ CHUYÊN GIA")));
        mvc.perform(get("/expert/dashboard").with(user(expert))).andExpect(status().isOk());
        mvc.perform(get("/expert/review/7").with(user(expert))).andExpect(status().isOk());
        mvc.perform(get("/admin/dashboard").with(user(admin))).andExpect(status().isOk());
        mvc.perform(get("/valuation/7/detail").with(user(admin))).andExpect(status().isOk());
        mvc.perform(get("/valuation/7/result").with(user(expert))).andExpect(status().isOk());
    }
}
