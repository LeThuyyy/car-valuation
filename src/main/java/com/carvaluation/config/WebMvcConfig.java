package com.carvaluation.config;

import com.carvaluation.storage.StorageProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final StorageProperties storageProperties;

    public WebMvcConfig(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = storageProperties.getRootLocation();
        String uploadPath = uploadDir.toUri().toString();

        // Ánh xạ URL /uploads/** trực tiếp đến thư mục uploads trên ổ cứng
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath + "/");
    }
}
