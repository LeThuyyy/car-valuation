package com.carvaluation.valuation;

import com.carvaluation.auth.User;
import com.carvaluation.damage.DamageAnalysisService;
import com.carvaluation.damage.DamageReport;
import com.carvaluation.pricing.PricingService;
import com.carvaluation.pricing.ValuationResult;
import com.carvaluation.storage.FileStorageService;
import com.carvaluation.vehicle.Vehicle;
import com.carvaluation.vehicle.VehicleRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ValuationService {

    private final ValuationSessionRepository sessionRepository;
    private final CarPhotoRepository photoRepository;
    private final VehicleRepository vehicleRepository;
    private final FileStorageService storageService;
    private final DamageAnalysisService damageAnalysisService;
    private final PricingService pricingService;
    private final ObjectMapper objectMapper;

    public ValuationService(ValuationSessionRepository sessionRepository,
                            CarPhotoRepository photoRepository,
                            VehicleRepository vehicleRepository,
                            FileStorageService storageService,
                            DamageAnalysisService damageAnalysisService,
                            PricingService pricingService,
                            ObjectMapper objectMapper) {
        this.sessionRepository = sessionRepository;
        this.photoRepository = photoRepository;
        this.vehicleRepository = vehicleRepository;
        this.storageService = storageService;
        this.damageAnalysisService = damageAnalysisService;
        this.pricingService = pricingService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ValuationSession startNewSession(User user) {
        ValuationSession session = new ValuationSession();
        session.setUser(user);
        session.setStatus(ValuationStatus.DRAFT);
        return sessionRepository.save(session);
    }

    public Optional<ValuationSession> getSessionForUser(Long sessionId, User user) {
        return sessionRepository.findByIdAndUserId(sessionId, user.getId());
    }

    public Optional<ValuationSession> getSessionById(Long sessionId) {
        return sessionRepository.findById(sessionId);
    }

    public List<ValuationSession> getUserHistory(User user) {
        return sessionRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional
    public ValuationSession updateVehicleInfo(Long sessionId, Vehicle vehicleData, User user) {
        ValuationSession session = sessionRepository.findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên định giá"));

        Vehicle vehicle = session.getVehicle();
        if (vehicle == null) {
            vehicle = new Vehicle();
            vehicle.setUser(user);
        }

        vehicle.setMake(vehicleData.getMake());
        vehicle.setModel(vehicleData.getModel());
        vehicle.setYear(vehicleData.getYear());
        vehicle.setTrim(vehicleData.getTrim());
        vehicle.setBodyType(vehicleData.getBodyType());
        vehicle.setTransmission(vehicleData.getTransmission());
        vehicle.setFuelType(vehicleData.getFuelType());
        vehicle.setColor(vehicleData.getColor());
        vehicle.setEngine(vehicleData.getEngine());
        vehicle.setLicensePlate(vehicleData.getLicensePlate());
        vehicle.setOrigin(vehicleData.getOrigin());
        if (session.getVinScanned() != null) {
            vehicle.setVin(session.getVinScanned());
        }

        vehicle = vehicleRepository.save(vehicle);
        session.setVehicle(vehicle);

        return sessionRepository.save(session);
    }

    @Transactional
    public ValuationSession updateOdo(Long sessionId, Integer mileage, User user) {
        ValuationSession session = sessionRepository.findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên định giá"));

        session.setOdoMileage(mileage);
        if (session.getVehicle() != null) {
            session.getVehicle().setMileage(mileage);
            vehicleRepository.save(session.getVehicle());
        }

        return sessionRepository.save(session);
    }

    @Transactional
    public CarPhoto uploadAndAnalyzePhoto(Long sessionId, PhotoType photoType, MultipartFile file, User user) {
        ValuationSession session = sessionRepository.findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên định giá"));

        String storedPath = storageService.store(file, "cars");
        byte[] bytes = storageService.loadAsBytes(storedPath);

        // Phân tích hư hỏng bằng Gemini Vision
        DamageReport report = damageAnalysisService.analyzePhoto(bytes, file.getContentType(), photoType.getTitle());

        CarPhoto photo = new CarPhoto();
        photo.setValuationSession(session);
        photo.setPhotoType(photoType);
        photo.setFilePath(storedPath);
        photo.setDetectedDamagesCount(report.getDamages() != null ? report.getDamages().size() : 0);

        try {
            photo.setAiAnalysisJson(objectMapper.writeValueAsString(report));
        } catch (Exception ignored) {}

        CarPhoto savedPhoto = photoRepository.save(photo);
        session.getPhotos().add(savedPhoto);
        sessionRepository.save(session);

        return savedPhoto;
    }

    @Transactional
    public ValuationSession processFinalValuation(Long sessionId, Map<String, Object> questionnaire, User user) {
        ValuationSession session = sessionRepository.findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên định giá"));

        if (session.getVehicle() == null) {
            throw new IllegalStateException("Hồ sơ xe chưa có thông tin cơ bản để định giá.");
        }

        // 1. Thu thập tất cả các kết quả phân tích ảnh đã có
        List<DamageReport> reports = new ArrayList<>();
        for (CarPhoto photo : session.getPhotos()) {
            if (photo.getAiAnalysisJson() != null && !photo.getAiAnalysisJson().isBlank()) {
                try {
                    DamageReport dr = objectMapper.readValue(photo.getAiAnalysisJson(), DamageReport.class);
                    reports.add(dr);
                } catch (Exception ignored) {}
            }
        }

        DamageReport combinedDamage = damageAnalysisService.combineReports(reports);

        // 2. Chạy Java Pricing Engine
        ValuationResult result = pricingService.evaluate(
                session.getVehicle(), session.getOdoMileage(), combinedDamage, questionnaire);

        // 3. Cập nhật kết quả vào phiên định giá
        session.setEstimatedPriceVnd(result.getEstimatedPriceVnd());
        session.setPriceRangeMinVnd(result.getPriceRangeMinVnd());
        session.setPriceRangeMaxVnd(result.getPriceRangeMaxVnd());
        session.setBaseMarketPriceVnd(result.getBaseMarketPriceVnd());
        session.setMileageAdjustmentVnd(result.getMileageAdjustmentVnd());
        session.setDamageAdjustmentVnd(result.getDamageAdjustmentVnd());
        session.setQuestionnaireAdjustmentVnd(result.getQuestionnaireAdjustmentVnd());
        session.setStatus(combinedDamage.isNeedsExpertReview() ? ValuationStatus.EXPERT_REVIEWING : ValuationStatus.AI_EVALUATED);

        try {
            session.setQuestionnaireJson(objectMapper.writeValueAsString(questionnaire));
            session.setAiReportJson(objectMapper.writeValueAsString(result));
        } catch (Exception ignored) {}

        return sessionRepository.save(session);
    }

    @Transactional
    public ValuationSession expertReview(Long sessionId, User expert, Long adjustedPrice, String notes) {
        ValuationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên định giá"));

        session.setAssignedExpert(expert);
        session.setExpertAdjustedPriceVnd(adjustedPrice);
        session.setExpertNotes(notes);
        session.setExpertReviewedAt(LocalDateTime.now());
        session.setStatus(ValuationStatus.COMPLETED);

        return sessionRepository.save(session);
    }
}
