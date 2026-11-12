package com.carvaluation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** No public upload resource handler: photos require per-session authorization. */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
}
