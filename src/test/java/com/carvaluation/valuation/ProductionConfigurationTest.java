package com.carvaluation.valuation;

import com.carvaluation.auth.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("prod")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:production-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "app.storage.upload-dir=target/test-prod-uploads",
        "gemini.api.key=test-only-not-a-real-key"
})
class ProductionConfigurationTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;

    @Test
    void productionDoesNotSeedAccountsOrPublishDemoCredentials() throws Exception {
        assertEquals(0, users.count());
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bắt đầu định giá xe của bạn")))
                .andExpect(content().string(not(containsString("Tài Khoản Thử Nghiệm Sẵn Có"))));
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Tài khoản thử nghiệm:"))));
    }

    @Test
    void productionDeniesH2Console() throws Exception {
        mvc.perform(get("/h2-console/").with(user("viewer"))).andExpect(status().isForbidden());
    }
}
