package com.carvaluation.valuation;

import com.carvaluation.ai.GeminiClient;
import com.carvaluation.ai.GeminiConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertThrows;

class GeminiProductionSafetyTest {

    @Test
    void productionRefusesMissingKeyAndMockMode() {
        GeminiConfig missingKey = new GeminiConfig();
        MockEnvironment production = new MockEnvironment();
        production.setActiveProfiles("prod");
        assertThrows(IllegalStateException.class,
                () -> new GeminiClient(missingKey, new ObjectMapper(), production));

        GeminiConfig mockEnabled = new GeminiConfig();
        mockEnabled.setKey("test-key");
        mockEnabled.setMockMode(true);
        assertThrows(IllegalStateException.class,
                () -> new GeminiClient(mockEnabled, new ObjectMapper(), production));
    }
}
