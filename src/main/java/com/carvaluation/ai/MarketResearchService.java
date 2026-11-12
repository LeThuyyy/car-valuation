package com.carvaluation.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.core.env.Environment;

import java.util.Arrays;

import java.util.HashMap;
import java.util.Map;

@Service
public class MarketResearchService {

    private static final Logger log = LoggerFactory.getLogger(MarketResearchService.class);

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;
    private final boolean production;

    public MarketResearchService(GeminiClient geminiClient, ObjectMapper objectMapper, Environment environment) {
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
        this.production = Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }

    public Map<String, Object> researchMarketPrice(String make, String model, Integer year, String trim) {
        String prompt = String.format("""
            Bạn là chuyên gia phân tích giá thị trường xe ô tô cũ tại Việt Nam.
            Hãy ước lượng khoảng giá giao dịch thực tế trên các sàn mua bán xe cũ (Bonbanh, Oto.com.vn, Chợ Tốt Xe) cho xe:
            - Hãng: %s
            - Dòng xe: %s
            - Năm sản xuất: %s
            - Phiên bản: %s

            Trả về JSON duy nhất theo mẫu:
            {
              "marketPriceMin": 450000000,
              "marketPriceMax": 490000000,
              "marketPriceAvg": 470000000,
              "sampleSize": 18,
              "priceTrend": "STABLE",
              "notes": "Nhận định ngắn gọn về thanh khoản dòng xe này tại Việt Nam"
            }
            """, make, model, year, trim != null ? trim : "");

        Map<String, Object> result = new HashMap<>();
        try {
            String jsonText = geminiClient.generateTextContent(prompt);
            JsonNode root = objectMapper.readTree(cleanJsonMarkdown(jsonText));
            if (production && (!root.path("marketPriceMin").isNumber()
                    || !root.path("marketPriceMax").isNumber()
                    || !root.path("marketPriceAvg").isNumber()
                    || root.path("marketPriceMin").asLong() <= 0
                    || root.path("marketPriceMin").asLong() > root.path("marketPriceAvg").asLong()
                    || root.path("marketPriceAvg").asLong() > root.path("marketPriceMax").asLong())) {
                throw new IllegalArgumentException("Invalid market price response");
            }

            result.put("marketPriceMin", root.path("marketPriceMin").asLong(400_000_000L));
            result.put("marketPriceMax", root.path("marketPriceMax").asLong(450_000_000L));
            result.put("marketPriceAvg", root.path("marketPriceAvg").asLong(425_000_000L));
            result.put("priceTrend", root.path("priceTrend").asText("STABLE"));
            result.put("notes", root.path("notes").asText("Thanh khoản tốt tại thị trường Việt Nam"));
        } catch (Exception e) {
            log.warn("Market research parsing failed: {}", e.getMessage());
            if (!production) result.put("marketPriceAvg", 450_000_000L);
        }

        return result;
    }

    private String cleanJsonMarkdown(String raw) {
        if (raw == null) return "{}";
        String s = raw.trim();
        if (s.startsWith("```json")) {
            s = s.substring(7);
        } else if (s.startsWith("```")) {
            s = s.substring(3);
        }
        if (s.endsWith("```")) {
            s = s.substring(0, s.length() - 3);
        }
        return s.trim();
    }
}
