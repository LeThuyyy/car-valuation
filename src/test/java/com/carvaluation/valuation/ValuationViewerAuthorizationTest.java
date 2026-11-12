package com.carvaluation.valuation;

import com.carvaluation.auth.Role;
import com.carvaluation.auth.User;
import com.carvaluation.damage.DamageAnalysisService;
import com.carvaluation.pricing.PricingService;
import com.carvaluation.storage.FileStorageService;
import com.carvaluation.vehicle.Vehicle;
import com.carvaluation.vehicle.VehicleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValuationViewerAuthorizationTest {
    @Mock ValuationSessionRepository sessions;
    @Mock CarPhotoRepository photos;
    @Mock VehicleRepository vehicles;
    @Mock FileStorageService storage;
    @Mock DamageAnalysisService damage;
    @Mock PricingService pricing;
    @Mock ObjectMapper mapper;
    @InjectMocks ValuationService service;

    private User viewer(long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }

    @Test
    void vinIsSavedWithVehicleInOneUpdate() {
        User owner = viewer(1, Role.USER);
        ValuationSession session = new ValuationSession();
        session.setUser(owner);
        when(sessions.findByIdAndUserId(7L, 1L)).thenReturn(Optional.of(session));
        when(vehicles.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(sessions.save(session)).thenReturn(session);
        Vehicle vehicle = new Vehicle();
        vehicle.setMake("Toyota");

        ValuationSession saved = service.updateVinAndVehicleInfo(7L, "1HGCM82633A004352", vehicle, owner);
        assertEquals("1HGCM82633A004352", saved.getVinScanned());
        assertEquals("1HGCM82633A004352", saved.getVehicle().getVin());
        verify(sessions, times(1)).findByIdAndUserId(7L, 1L);
        verify(sessions).save(session);
    }

    @Test
    void onlyOwnerOrStaffMayAccessReports() {
        ValuationSession report = new ValuationSession();
        User owner = viewer(1, Role.USER);
        report.setUser(owner);
        when(sessions.findByIdAndUserId(7L, 1L)).thenReturn(Optional.of(report));
        when(sessions.findByIdAndUserId(7L, 9L)).thenReturn(Optional.empty());
        when(sessions.findById(7L)).thenReturn(Optional.of(report));

        assertSame(report, service.getSessionForViewer(7L, owner).orElseThrow());
        assertTrue(service.getSessionForViewer(7L, viewer(9, Role.USER)).isEmpty());
        assertSame(report, service.getSessionForViewer(7L, viewer(2, Role.EXPERT)).orElseThrow());
        assertSame(report, service.getSessionForViewer(7L, viewer(3, Role.ADMIN)).orElseThrow());
        assertTrue(service.getSessionForViewer(7L, null).isEmpty());
    }
}
