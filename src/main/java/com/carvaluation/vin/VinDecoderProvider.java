package com.carvaluation.vin;

import java.util.Optional;

/**
 * Interface cho phép linh hoạt cắm rút nhiều nhà cung cấp giải mã VIN
 * (NHTSA API, Vietnam Market Rule Provider, Third-party VIN APIs...)
 */
public interface VinDecoderProvider {
    String getProviderName();
    boolean supports(String vin);
    Optional<VinScanResult> decode(String vin);
    int getOrder(); // Độ ưu tiên (số nhỏ hơn ưu tiên chạy trước)
}
