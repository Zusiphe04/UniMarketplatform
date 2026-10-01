package com.example.unimarket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Supplies the password encoder.
 *
 * <p>BCrypt is adaptive and salts each hash automatically, so two members with
 * the same password produce different stored values and a precomputed table is
 * useless against them.
 *
 * <p>Strength 12 is chosen over the default 10 to raise the cost per guess.
 * Comparison always runs through {@link PasswordEncoder#matches}, which is
 * constant-time and therefore does not leak information through response timing.
 */
@Configuration
public class PasswordEncoderConfig {

    private static final int BCRYPT_STRENGTH = 12;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCRYPT_STRENGTH);
    }
}
