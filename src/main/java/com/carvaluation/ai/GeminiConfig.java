package com.carvaluation.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gemini.api")
public class GeminiConfig {

    private String key = "";
    private String baseUrl = "https://generativelanguage.googleapis.com/v1beta";
    private String visionModel = "gemini-1.5-flash";
    private String textModel = "gemini-1.5-flash";
    private boolean mockMode = false; // Khi chưa có API key thật, hệ thống tự động chạy Mock AI thông minh để test luồng hoàn hảo

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getVisionModel() {
        return visionModel;
    }

    public void setVisionModel(String visionModel) {
        this.visionModel = visionModel;
    }

    public String getTextModel() {
        return textModel;
    }

    public void setTextModel(String textModel) {
        this.textModel = textModel;
    }

    public boolean isMockMode() {
        return mockMode || key == null || key.isBlank() || key.contains("YOUR_GEMINI_API_KEY");
    }

    public void setMockMode(boolean mockMode) {
        this.mockMode = mockMode;
    }
}
