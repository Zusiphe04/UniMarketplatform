package com.example.unimarket.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.List;

@Configuration
public class JwtConfig {
    private SecretKey createSecretKey(String encodedSecret) {
        byte[] keyBytes;
        try { keyBytes = Base64.getDecoder().decode(encodedSecret); }
        catch (IllegalArgumentException exception) {
            throw new IllegalStateException("security.jwt.secret must be a valid Base64 value", exception);
        }
        if (keyBytes.length < 32) throw new IllegalStateException("The JWT secret must contain at least 32 bytes");
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(@Value("${security.jwt.secret}") String encodedSecret) {
        SecretKey secretKey = createSecretKey(encodedSecret);
        JWKSource<SecurityContext> jwkSource = new ImmutableSecret<>(secretKey);
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(@Value("${security.jwt.secret}") String encodedSecret,
                                 @Value("${security.jwt.issuer}") String issuer,
                                 @Value("${security.jwt.audience}") String audience,
                                 ActiveSessionJwtValidator activeSessionJwtValidator) {
        SecretKey secretKey = createSecretKey(encodedSecret);
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();
        OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> audienceValidator = new JwtClaimValidator<List<String>>("aud",
                audiences -> audiences != null && audiences.contains(audience));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                issuerValidator, audienceValidator, activeSessionJwtValidator));
        return decoder;
    }
}
