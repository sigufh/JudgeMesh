package com.judgemesh.gateway.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
public class JwtVerifier {
    private static final Base64.Encoder B64_ENC = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder B64_DEC = Base64.getUrlDecoder();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;
    private final String secret;

    public JwtVerifier(
            ObjectMapper objectMapper,
            @Value("${judgemesh.jwt.secret:local-dev-secret-change-me-32-bytes}") String secret) {
        this.objectMapper = objectMapper;
        this.secret = secret;
    }

    public Claims verify(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("bad token format");
            }
            String signingInput = parts[0] + "." + parts[1];
            if (!java.security.MessageDigest.isEqual(
                    sign(signingInput).getBytes(StandardCharsets.UTF_8),
                    parts[2].getBytes(StandardCharsets.UTF_8))) {
                throw new IllegalArgumentException("bad token signature");
            }
            Map<String, Object> payload = objectMapper.readValue(B64_DEC.decode(parts[1]), MAP_TYPE);
            long exp = ((Number) payload.get("exp")).longValue();
            if (Instant.ofEpochSecond(exp).isBefore(Instant.now())) {
                throw new IllegalArgumentException("token expired");
            }
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) payload.getOrDefault("roles", List.of());
            return new Claims(Long.valueOf(payload.get("sub").toString()), payload.get("username").toString(), roles);
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid token", ex);
        }
    }

    private String sign(String signingInput) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return B64_ENC.encodeToString(mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("cannot sign jwt", ex);
        }
    }

    public record Claims(Long userId, String username, List<String> roles) {
    }
}
