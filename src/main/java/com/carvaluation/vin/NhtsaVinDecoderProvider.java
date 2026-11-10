package com.carvaluation.vin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

/**
 * Nhà cung cấp giải mã VIN qua API vPIC của NHTSA (National Highway Traffic Safety Administration)
 * Thích hợp cho xe sản xuất quốc tế hoặc nhập khẩu.
 */
@Component
public class NhtsaVinDecoderProvider implements VinDecoderProvider {

    private static final Logger log = LoggerFactory.getLogger(NhtsaVinDecoderProvider.class);
    private static final String NHTSA_URL = "https://vpic.nhtsa.dot.gov/api/vehicles/decodevinvalues/%s?format=json";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public NhtsaVinDecoderProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public String getProviderName() {
        return "NHTSA_VPIC_API";
    }

    @Override
    public boolean supports(String vin) {
        return vin != null && vin.length() == 17;
    }

    @Override
    public Optional<VinScanResult> decode(String vin) {
        try {
            String url = String.format(NHTSA_URL, vin);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(7))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode results = root.path("Results");

                if (results.isArray() && !results.isEmpty()) {
                    JsonNode node = results.get(0);

                    String make = getTextOrNull(node, "Make");
                    String model = getTextOrNull(node, "Model");
                    String yearStr = getTextOrNull(node, "ModelYear");

                    if (make != null && !make.isBlank() && model != null && !model.isBlank()) {
                        VinScanResult result = new VinScanResult();
                        result.setSuccess(true);
                        result.setValid(true);
                        result.setVin(vin);
                        result.setMake(make);
                        result.setModel(model);

                        if (yearStr != null && !yearStr.isBlank()) {
                            try {
                                result.setYear(Integer.parseInt(yearStr));
                            } catch (NumberFormatException ignored) {}
                        }

                        result.setTrim(getTextOrNull(node, "Trim"));
                        result.setBodyType(getTextOrNull(node, "BodyClass"));
                        result.setTransmission(getTextOrNull(node, "TransmissionStyle"));
                        result.setFuelType(getTextOrNull(node, "FuelTypePrimary"));
                        result.setEngine(getTextOrNull(node, "DisplacementL") != null ? node.path("DisplacementL").asText() + "L" : null);
                        result.setCountry(getTextOrNull(node, "PlantCountry"));
                        result.setOrigin(result.getCountry() != null ? "Nhập khẩu (" + result.getCountry() + ")" : "Nhập khẩu");
                        result.setSource(getProviderName());
                        result.setNeedsConfirmation(true);
                        result.setMessage("Giải mã thành công từ cơ sở dữ liệu quốc tế NHTSA");

                        return Optional.of(result);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("NHTSA VIN decode failed or timed out for VIN {}: {}", vin, e.getMessage());
        }
        return Optional.empty();
    }

    private String getTextOrNull(JsonNode node, String field) {
        if (node.hasNonNull(field)) {
            String text = node.get(field).asText().trim();
            if (!text.isEmpty() && !text.equalsIgnoreCase("Not Applicable") && !text.equalsIgnoreCase("N/A")) {
                return text;
            }
        }
        return null;
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
