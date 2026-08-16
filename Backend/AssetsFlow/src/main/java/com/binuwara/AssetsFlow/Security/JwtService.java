package com.binuwara.AssetsFlow.Security;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class JwtService {
    private static final String SIGNING_ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private final JsonMapper objectMapper;
    private final SecretKeySpec signingKey;
    private final long expirationMinutes;

    public JwtService(
            JsonMapper objectMapper,
            @Value("${app.security.jwt-secret}") String secret,
            @Value("${app.security.jwt-expiration-minutes:30}") long expirationMinutes
    ) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 bytes.");
        }
        if (expirationMinutes <= 0) {
            throw new IllegalArgumentException("JWT expiration must be positive.");
        }

        this.objectMapper = objectMapper;
        this.signingKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                SIGNING_ALGORITHM
        );
        this.expirationMinutes = expirationMinutes;
    }

    public JwtToken createToken(UUID subject) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(expirationMinutes * 60);

        ObjectNode header = objectMapper.createObjectNode()
                .put("alg", "HS256")
                .put("typ", "JWT");
        ObjectNode payload = objectMapper.createObjectNode()
                .put("sub", subject.toString())
                .put("iat", issuedAt.getEpochSecond())
                .put("exp", expiresAt.getEpochSecond());

        String encodedHeader = encodeJson(header);
        String encodedPayload = encodeJson(payload);
        String unsignedToken = encodedHeader + "." + encodedPayload;
        String signature = ENCODER.encodeToString(sign(unsignedToken));

        return new JwtToken(unsignedToken + "." + signature, expiresAt);
    }

    public JwtClaims parseAndValidate(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new InvalidJwtException();
            }

            String unsignedToken = parts[0] + "." + parts[1];
            byte[] expectedSignature = sign(unsignedToken);
            byte[] providedSignature = DECODER.decode(parts[2]);
            if (!MessageDigest.isEqual(expectedSignature, providedSignature)) {
                throw new InvalidJwtException();
            }

            JsonNode payload = objectMapper.readTree(DECODER.decode(parts[1]));
            UUID subject = UUID.fromString(payload.path("sub").asText());
            long expiration = payload.path("exp").asLong(0);
            Instant expiresAt = Instant.ofEpochSecond(expiration);
            if (expiration == 0 || !expiresAt.isAfter(Instant.now())) {
                throw new InvalidJwtException();
            }

            return new JwtClaims(subject, expiresAt);
        } catch (InvalidJwtException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InvalidJwtException();
        }
    }

    private String encodeJson(JsonNode node) {
        try {
            return ENCODER.encodeToString(
                    objectMapper.writeValueAsString(node).getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Could not create authentication token.", exception);
        }
    }

    private byte[] sign(String value) {
        try {
            Mac mac = Mac.getInstance(SIGNING_ALGORITHM);
            mac.init(signingKey);
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not sign authentication token.", exception);
        }
    }

    public record JwtToken(String value, Instant expiresAt) {
    }

    public record JwtClaims(UUID subject, Instant expiresAt) {
    }

    public static class InvalidJwtException extends RuntimeException {
    }
}
