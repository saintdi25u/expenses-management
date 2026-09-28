package com.corentin.expenses.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.util.Base64;

@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(@NotBlank String secret, Duration expiration) {

    public JwtProperties {
        if (expiration == null) expiration = Duration.ofHours(1);
    }

    public SecretKey secretKey() {
        byte[] bytes = Base64.getDecoder().decode(secret);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret must be less 256 bits");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }
}