package com.judgemesh.user.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.judgemesh.user.domain.UserAccount;
import com.judgemesh.user.domain.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {
    private static final Base64.Encoder B64_ENC = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder B64_DEC = Base64.getUrlDecoder();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;
    private final String secret;
    private final long ttlSeconds;

    public JwtService(
            ObjectMapper objectMapper,
            @Value("${judgemesh.jwt.secret:local-dev-secret-change-me-32-bytes}") String secret,
            @Value("${judgemesh.jwt.ttl-seconds:86400}") long ttlSeconds) {
        this.objectMapper = objectMapper;
        this.secret = secret;
        this.ttlSeconds = ttlSeconds;
    }

    public String issue(UserAccount user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ttlSeconds);
        List<String> roles = user.getRoles().stream().map(UserRole::name).sorted().toList();
        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = Map.of(
                "sub", user.getId().toString(),
                "username", user.getUsername(),
                "roles", roles,
                "iat", now.getEpochSecond(),
                "exp", exp.getEpochSecond());
        String head = encodeJson(header);
        String body = encodeJson(payload);
        return head + "." + body + "." + sign(head + "." + body);
    }

    public Claims verify(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("bad token format");
            }
            String signingInput = parts[0] + "." + parts[1];
            if (!constantTimeEquals(sign(signingInput), parts[2])) {
                throw new IllegalArgumentException("bad token signature");
            }
            Map<String, Object> payload = objectMapper.readValue(B64_DEC.decode(parts[1]), MAP_TYPE);
            long exp = ((Number) payload.get("exp")).longValue();
            if (Instant.ofEpochSecond(exp).isBefore(Instant.now())) {
                throw new IllegalArgumentException("token expired");
            }
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) payload.getOrDefault("roles", List.of());
            return new Claims(
                    Long.valueOf(payload.get("sub").toString()),
                    payload.get("username").toString(),
                    roles,
                    Instant.ofEpochSecond(exp));
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid token", ex);
        }
    }

    public long ttlSeconds() {
        return ttlSeconds;
    }

    private String encodeJson(Object value) {
        try {
            return B64_ENC.encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (Exception ex) {
            throw new IllegalStateException("cannot encode jwt", ex);
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

    private static boolean constantTimeEquals(String a, String b) {
        return java.security.MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }

    public record Claims(Long userId, String username, List<String> roles, Instant expiresAt) {
    }
}
