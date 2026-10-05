package com.example.unimarket.config;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${unimarket.security.allowed-origins}") List<String> allowedOrigins) {
        CorsConfiguration application = new CorsConfiguration();
        application.setAllowedOrigins(allowedOrigins.stream().map(String::trim).filter(value -> !value.isEmpty()).toList());
        application.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        application.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Idempotency-Key"));
        application.setAllowCredentials(true);
        application.setMaxAge(Duration.ofHours(1));

        CorsConfiguration health = new CorsConfiguration();
        health.setAllowedOrigins(List.of("*"));
        health.setAllowedMethods(List.of("GET", "OPTIONS"));
        health.setAllowedHeaders(List.of("Accept"));
        health.setAllowCredentials(false);
        health.setMaxAge(Duration.ofHours(1));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/actuator/health", health);
        source.registerCorsConfiguration("/**", application);
        return source;
    }
}
