package com.tylerlam.expensetracker.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    // 32 bytes of 0x61 ('a') encoded as standard Base64 — valid 256-bit HS256 key
    private static final String TEST_SECRET = "YWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWE=";
    private static final long TEST_EXPIRATION = 3_600_000L; // 1 hour in ms

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, TEST_EXPIRATION);
        userDetails = User.withUsername("test@example.com")
                .password("password")
                .roles("USER")
                .build();
    }

    // --- generateToken ---

    @Test
    void generateToken_returnsNonEmptyToken() {
        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotBlank();
    }

    @Test
    void generateToken_subjectMatchesUsername() {
        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.extractUsername(token)).isEqualTo("test@example.com");
    }

    @Test
    void generateToken_withExtraClaims_extraClaimsArePresentInToken() {
        Map<String, Object> extraClaims = Map.of("role", "ADMIN", "userId", 42);

        String token = jwtService.generateToken(extraClaims, userDetails);
        Claims claims = parseClaims(token);

        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
        assertThat(claims.get("userId", Integer.class)).isEqualTo(42);
    }

    @Test
    void generateToken_issuedAtIsSetToNow() {
        long before = System.currentTimeMillis();
        String token = jwtService.generateToken(userDetails);

        Claims claims = parseClaims(token);
        long issuedAt = claims.getIssuedAt().getTime();

        // JWT stores timestamps as epoch seconds, so issuedAt may be truncated by up to 999ms
        assertThat(issuedAt).isBetween(before - 1000L, before + 1000L);
    }

    @Test
    void generateToken_expirationIsSetCorrectly() {
        String token = jwtService.generateToken(userDetails);

        Claims claims = parseClaims(token);
        long issuedAt = claims.getIssuedAt().getTime();
        long expiration = claims.getExpiration().getTime();

        // Allow 1 second of slack since both timestamps are truncated to seconds independently
        assertThat(expiration - issuedAt).isBetween(TEST_EXPIRATION, TEST_EXPIRATION + 1000L);
        assertThat(expiration).isGreaterThan(issuedAt);
    }

    // --- extractUsername ---

    @Test
    void extractUsername_returnsCorrectUsername() {
        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.extractUsername(token)).isEqualTo("test@example.com");
    }

    @Test
    void extractUsername_throwsExpiredJwtException_whenTokenIsExpired() {
        JwtService expiredService = new JwtService(TEST_SECRET, -1000L);
        String expiredToken = expiredService.generateToken(userDetails);

        assertThatThrownBy(() -> jwtService.extractUsername(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void extractUsername_throwsException_whenTokenIsMalformed() {
        assertThatThrownBy(() -> jwtService.extractUsername("not.a.valid.token"))
                .isInstanceOf(Exception.class);
    }

    // --- isTokenValid ---

    @Test
    void isTokenValid_returnsTrue_whenTokenMatchesUserAndIsNotExpired() {
        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void isTokenValid_returnsFalse_whenTokenBelongsToDifferentUser() {
        String token = jwtService.generateToken(userDetails);
        UserDetails otherUser = User.withUsername("other@example.com")
                .password("password")
                .roles("USER")
                .build();

        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    void isTokenValid_throwsExpiredJwtException_whenTokenIsExpired() {
        JwtService expiredService = new JwtService(TEST_SECRET, -1000L);
        String expiredToken = expiredService.generateToken(userDetails);

        assertThatThrownBy(() -> jwtService.isTokenValid(expiredToken, userDetails))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void extractUsername_throwsException_whenTokenIsSignedWithWrongKey() {
        // 32 'b' bytes — valid key length but different from TEST_SECRET
        String differentSecret = "YmJiYmJiYmJiYmJiYmJiYmJiYmJiYmJiYmJiYmJiYmI=";
        JwtService otherService = new JwtService(differentSecret, TEST_EXPIRATION);
        String tokenFromOtherKey = otherService.generateToken(userDetails);

        assertThatThrownBy(() -> jwtService.extractUsername(tokenFromOtherKey))
                .isInstanceOf(Exception.class);
    }

    // --- helpers ---

    private Claims parseClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
