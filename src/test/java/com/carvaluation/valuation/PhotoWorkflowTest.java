package com.carvaluation.valuation;

import com.carvaluation.auth.CustomUserDetails;
import com.carvaluation.auth.Role;
import com.carvaluation.auth.SecurityConfig;
import com.carvaluation.auth.User;
import com.carvaluation.storage.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {ValuationController.class, PhotoController.class})
@Import(SecurityConfig.class)
class PhotoWorkflowTest {
    @Autowired MockMvc mvc;
    @MockBean ValuationService valuationService;
    @MockBean com.carvaluation.vin.VinDecoderService vinDecoderService;
    @MockBean FileStorageService storageService;

    private CustomUserDetails signedIn(Long id, Role role) {
        User person = new User();
        person.setId(id);
        person.setUsername("viewer" + id);
        person.setPassword("hashed-for-test");
        person.setRole(role);
        return new CustomUserDetails(person);
    }

    @Test
    void photoPageIncludesUploadScriptAndCsrfToken() throws Exception {
        CustomUserDetails owner = signedIn(1L, Role.USER);
        ValuationSession session = new ValuationSession();
        session.setId(7L);
        session.setUser(owner.getUser());
        when(valuationService.getSessionForUser(7L, owner.getUser()))
                .thenReturn(Optional.of(session));

        mvc.perform(get("/valuation/7/photos").with(user(owner)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/photos/upload")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/valuation/7/questionnaire")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"_csrf\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"_csrf_header\"")));
    }

    @Test
    void uploadRequiresCsrf() throws Exception {
        MockMultipartFile image = new MockMultipartFile("file", "front.jpg", "image/jpeg", new byte[]{1});
        mvc.perform(multipart("/valuation/7/photos/upload")
                        .file(image).param("photoType", "FRONT").with(user(signedIn(1L, Role.USER))))
                .andExpect(status().isForbidden());
        verifyNoInteractions(valuationService);
    }

    @Test
    void uploadReturnsOnlyDtoFields() throws Exception {
        CustomUserDetails principal = signedIn(1L, Role.USER);
        ValuationSession session = new ValuationSession();
        session.setUser(principal.getUser());
        CarPhoto photo = new CarPhoto(session, PhotoType.FRONT, "cars/example.jpg");
        photo.setId(44L);
        photo.setAiAnalysisJson("{\"damages\":[]}");
        session.setPhotos(List.of(photo));
        when(valuationService.uploadAndAnalyzePhoto(eq(7L), eq(PhotoType.FRONT), any(), any())).thenReturn(photo);
        MockMultipartFile image = new MockMultipartFile("file", "front.jpg", "image/jpeg", new byte[]{1});

        String json = mvc.perform(multipart("/valuation/7/photos/upload")
                        .file(image).param("photoType", "FRONT").with(user(principal)).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.photoUrl").value("/valuation/photos/44/image"))
                .andExpect(jsonPath("$.data.damageReportJson").value("{\"damages\":[]}"))
                .andExpect(jsonPath("$.data.valuationSession").doesNotExist())
                .andExpect(jsonPath("$.data.filePath").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        assertEquals(false, json.contains("hashed-for-test"));
    }

    @Test
    void photoRequiresAuthenticationAndOwnerAuthorization() throws Exception {
        mvc.perform(get("/valuation/photos/44/image"))
                .andExpect(status().is3xxRedirection());
        CustomUserDetails other = signedIn(2L, Role.USER);
        when(valuationService.getPhotoForViewer(44L, other.getUser())).thenReturn(Optional.empty());
        mvc.perform(get("/valuation/photos/44/image").with(user(other)))
                .andExpect(status().isNotFound());
        verifyNoInteractions(storageService);
    }

    @Test
    void authorizedPhotoReturnsImageWithoutPublicCaching() throws Exception {
        CustomUserDetails owner = signedIn(1L, Role.USER);
        CarPhoto photo = new CarPhoto(new ValuationSession(), PhotoType.FRONT, "cars/front.jpg");
        when(valuationService.getPhotoForViewer(44L, owner.getUser())).thenReturn(Optional.of(photo));
        when(storageService.getRootLocation()).thenReturn(Path.of("uploads").toAbsolutePath());
        when(storageService.loadAsBytes("cars/front.jpg")).thenReturn(new byte[]{1, 2, 3});

        mvc.perform(get("/valuation/photos/44/image").with(user(owner)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(header().string("Cache-Control", "private, no-store"))
                .andExpect(content().bytes(new byte[]{1, 2, 3}));
    }

    @Test
    void photoPathCannotEscapeUploadDirectory() throws Exception {
        CustomUserDetails owner = signedIn(1L, Role.USER);
        CarPhoto photo = new CarPhoto(new ValuationSession(), PhotoType.FRONT, "cars/../../other.jpg");
        when(valuationService.getPhotoForViewer(44L, owner.getUser())).thenReturn(Optional.of(photo));
        when(storageService.getRootLocation()).thenReturn(Path.of("uploads").toAbsolutePath());

        mvc.perform(get("/valuation/photos/44/image").with(user(owner)))
                .andExpect(status().isNotFound());
        org.mockito.Mockito.verify(storageService, org.mockito.Mockito.never())
                .loadAsBytes(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void publicUploadsUrlIsDenied() throws Exception {
        mvc.perform(get("/uploads/cars/example.jpg"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(get("/uploads/cars/example.jpg").with(user(signedIn(1L, Role.USER))))
                .andExpect(status().isForbidden());
    }
}
