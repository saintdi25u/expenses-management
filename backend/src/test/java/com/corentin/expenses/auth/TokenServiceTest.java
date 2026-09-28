package com.corentin.expenses.auth;

import com.corentin.expenses.entity.Role;
import com.corentin.expenses.entity.UserEntity;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class TokenServiceTest {

    private static final SecretKey KEY = new SecretKeySpec(new byte[32], "HmacSHA256");

    private TokenService tokenService;
    private JwtDecoder decoder;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService(new NimbusJwtEncoder(new ImmutableSecret<>(KEY)));
        decoder = NimbusJwtDecoder.withSecretKey(KEY).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Test
    void generate_producesSignedTokenWithUserClaims() {
        UserEntity user = user(42L);
        user.setRole(Role.ADMIN);

        Jwt jwt = decoder.decode(tokenService.generate(user));

        assertThat(jwt.getHeaders()).containsEntry("alg", "HS256");
        assertThat(jwt.getSubject()).isEqualTo("42");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("expenses-app");
        assertThat(jwt.getClaimAsString("email")).isEqualTo("john@doe.com");
        assertThat(jwt.getClaimAsString("scope")).isEqualTo("ADMIN");
    }

    @Test
    void generate_usesDefaultUserRoleAsScope() {
        Jwt jwt = decoder.decode(tokenService.generate(user(1L)));

        assertThat(jwt.getClaimAsString("scope")).isEqualTo("USER");
    }

    @Test
    void generate_setsOneHourExpiration() {
        Instant before = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        Jwt jwt = decoder.decode(tokenService.generate(user(1L)));

        assertThat(jwt.getIssuedAt()).isCloseTo(before, within(5, ChronoUnit.SECONDS));
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofHours(1));
    }

    @Test
    void generate_throws_whenUserHasNoId() {
        UserEntity user = user(null);

        assertThatThrownBy(() -> tokenService.generate(user)).isInstanceOf(NullPointerException.class);
    }

    private static UserEntity user(Long id) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setEmail("john@doe.com");
        return user;
    }
}
