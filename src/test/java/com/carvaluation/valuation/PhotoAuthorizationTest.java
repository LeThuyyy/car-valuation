package com.carvaluation.valuation;

import com.carvaluation.auth.Role;
import com.carvaluation.auth.User;
import com.carvaluation.damage.DamageAnalysisService;
import com.carvaluation.pricing.PricingService;
import com.carvaluation.storage.FileStorageService;
import com.carvaluation.vehicle.VehicleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhotoAuthorizationTest {
    @Mock ValuationSessionRepository sessions;
    @Mock CarPhotoRepository photos;
    @Mock VehicleRepository vehicles;
    @Mock FileStorageService storage;
    @Mock DamageAnalysisService damage;
    @Mock PricingService pricing;
    @Mock ObjectMapper mapper;
    @InjectMocks ValuationService service;

    private User user(long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }

    @Test
    void ownerAndStaffCanReadButOtherCustomerCannot() {
        User owner = user(1, Role.USER);
        ValuationSession session = new ValuationSession();
        session.setUser(owner);
        CarPhoto photo = new CarPhoto(session, PhotoType.FRONT, "cars/front.jpg");
        when(photos.findById(9L)).thenReturn(Optional.of(photo));

        assertSame(photo, service.getPhotoForViewer(9L, owner).orElseThrow());
        assertTrue(service.getPhotoForViewer(9L, user(2, Role.USER)).isEmpty());
        assertSame(photo, service.getPhotoForViewer(9L, user(3, Role.EXPERT)).orElseThrow());
        assertSame(photo, service.getPhotoForViewer(9L, user(4, Role.ADMIN)).orElseThrow());
    }

    @Test
    void missingPhotoAndMissingViewerDoNotGrantAccess() {
        assertTrue(service.getPhotoForViewer(9L, null).isEmpty());
        verifyNoInteractions(photos);
        assertTrue(service.getPhotoForViewer(9L, user(1, Role.USER)).isEmpty());
    }
}
