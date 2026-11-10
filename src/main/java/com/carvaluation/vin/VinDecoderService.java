package com.carvaluation.vin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class VinDecoderService {

    private static final Logger log = LoggerFactory.getLogger(VinDecoderService.class);

    private final VinValidator vinValidator;
    private final List<VinDecoderProvider> providers;

    public VinDecoderService(VinValidator vinValidator, List<VinDecoderProvider> providers) {
        this.vinValidator = vinValidator;
        // Sắp xếp các provider theo thứ tự ưu tiên
        this.providers = providers.stream()
                .sorted(Comparator.comparingInt(VinDecoderProvider::getOrder))
                .toList();
    }

    public VinScanResult decodeVin(String rawVin) {
        String vin = vinValidator.clean(rawVin);

        if (!vinValidator.isValid(vin)) {
            String reason = "Số VIN phải có đúng 17 ký tự (không bao gồm các chữ I, O, Q).";
            if (vin.length() != 17) {
                reason = "Số VIN hiện có " + vin.length() + "/17 ký tự. Vui lòng kiểm tra lại.";
            } else if (vin.matches(".*[IOQ].*")) {
                reason = "Số VIN theo tiêu chuẩn không được chứa các ký tự I, O, Q để tránh nhầm với 1 và 0.";
            }
            return VinScanResult.failure(vin, reason);
        }

        VinScanResult finalResult = new VinScanResult();
        finalResult.setVin(vin);
        finalResult.setValid(true);
        finalResult.setSuccess(true);
        finalResult.setNeedsConfirmation(true);

        boolean decodedAny = false;

        // Chạy qua các provider để giải mã và kết hợp dữ liệu (Composite merge)
        for (VinDecoderProvider provider : providers) {
            if (provider.supports(vin)) {
                try {
                    Optional<VinScanResult> opt = provider.decode(vin);
                    if (opt.isPresent()) {
                        VinScanResult res = opt.get();
                        mergeResult(finalResult, res);
                        decodedAny = true;

                        // Nếu đã đủ thông tin cốt lõi (Make, Model, Year) thì có thể dừng
                        if (finalResult.getMake() != null && finalResult.getModel() != null && finalResult.getYear() != null) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error running provider {}: {}", provider.getProviderName(), e.getMessage());
                }
            }
        }

        // Kiểm tra các trường thông tin còn thiếu
        List<String> missing = new ArrayList<>();
        if (finalResult.getMake() == null) missing.add("Hãng xe (Make)");
        if (finalResult.getModel() == null) missing.add("Dòng xe (Model)");
        if (finalResult.getYear() == null) missing.add("Năm sản xuất (Year)");
        if (finalResult.getBodyType() == null) missing.add("Kiểu dáng (Sedan, SUV...)");
        if (finalResult.getTransmission() == null) missing.add("Hộp số (Tự động / Số sàn)");
        if (finalResult.getFuelType() == null) missing.add("Nhiên liệu (Xăng, Dầu, Điện...)");
        finalResult.setMissingFields(missing);

        if (!decodedAny) {
            finalResult.setSource("MANUAL");
            finalResult.setMessage("Không tìm thấy thông tin tự động cho số VIN này. Vui lòng nhập thông tin xe thủ công.");
        } else if (!missing.isEmpty()) {
            finalResult.setMessage("Đã giải mã được một phần thông tin xe từ VIN. Vui lòng xác nhận hoặc bổ sung các mục còn thiếu.");
        } else {
            finalResult.setMessage("Giải mã thành công đầy đủ thông tin xe từ VIN.");
        }

        return finalResult;
    }

    private void mergeResult(VinScanResult target, VinScanResult source) {
        if (target.getMake() == null && source.getMake() != null) target.setMake(source.getMake());
        if (target.getModel() == null && source.getModel() != null) target.setModel(source.getModel());
        if (target.getYear() == null && source.getYear() != null) target.setYear(source.getYear());
        if (target.getTrim() == null && source.getTrim() != null) target.setTrim(source.getTrim());
        if (target.getBodyType() == null && source.getBodyType() != null) target.setBodyType(source.getBodyType());
        if (target.getTransmission() == null && source.getTransmission() != null) target.setTransmission(source.getTransmission());
        if (target.getFuelType() == null && source.getFuelType() != null) target.setFuelType(source.getFuelType());
        if (target.getEngine() == null && source.getEngine() != null) target.setEngine(source.getEngine());
        if (target.getOrigin() == null && source.getOrigin() != null) target.setOrigin(source.getOrigin());
        if (target.getCountry() == null && source.getCountry() != null) target.setCountry(source.getCountry());
        if (target.getSource() == null && source.getSource() != null) target.setSource(source.getSource());
    }
}
