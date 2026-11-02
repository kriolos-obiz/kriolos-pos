/*
 * Copyright (C) 2026 KriolOS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.openbravo.pos.domain.audit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * Reusable enterprise integrity engine for canonical payload serialization (JSON)
 * and SHA-256 cryptographic checksum calculation.
 * Ensures Write Once, Read Many (WORM) tamper-evident auditability across sales and cash registers.
 *
 * @author poolborges
 */
public final class DocumentIntegrityService {

    private static final ObjectMapper CANONICAL_MAPPER = createCanonicalMapper();

    private DocumentIntegrityService() {
        // Utility class
    }

    /**
     * Creates and configures a deterministic, canonical Jackson ObjectMapper.
     * Enforces ISO-8601 timestamps, sorted map/property keys, and excludes null entries.
     *
     * @return configured ObjectMapper
     */
    public static ObjectMapper createCanonicalMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    /**
     * Serializes any document record/DTO into a canonical, sorted JSON string.
     *
     * @param documentPayload object to serialize
     * @return canonical JSON string
     * @throws DocumentSerializationException if serialization fails
     */
    public static String serializeCanonicalJson(Object documentPayload) {
        Objects.requireNonNull(documentPayload, "documentPayload must not be null");
        try {
            return CANONICAL_MAPPER.writeValueAsString(documentPayload);
        } catch (JsonProcessingException e) {
            throw new DocumentSerializationException("Failed to serialize document payload canonically", e);
        }
    }

    /**
     * Deserializes a canonical JSON string back into the target document type.
     *
     * @param json canonical JSON string
     * @param targetClass target class type
     * @param <T> type
     * @return deserialized instance
     * @throws DocumentSerializationException if deserialization fails
     */
    public static <T> T deserializeJson(String json, Class<T> targetClass) {
        Objects.requireNonNull(json, "json must not be null");
        Objects.requireNonNull(targetClass, "targetClass must not be null");
        try {
            return CANONICAL_MAPPER.readValue(json, targetClass);
        } catch (JsonProcessingException e) {
            throw new DocumentSerializationException("Failed to deserialize canonical JSON payload into " + targetClass.getName(), e);
        }
    }

    /**
     * Calculates the lowercase hexadecimal SHA-256 cryptographic digest of a payload.
     *
     * @param payload UTF-8 text payload
     * @return 64-character lowercase hex string
     */
    public static String computeSha256(String payload) {
        Objects.requireNonNull(payload, "payload must not be null");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(64);
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available in current JVM", e);
        }
    }

    /**
     * Validates if a document payload matches its cryptographic checksum.
     *
     * @param payload document text payload
     * @param expectedChecksum expected 64-char SHA-256 hash
     * @return true if valid, false if tampered or mismatched
     */
    public static boolean verifyIntegrity(String payload, String expectedChecksum) {
        if (payload == null || expectedChecksum == null) {
            return false;
        }
        String calculated = computeSha256(payload);
        return calculated.equalsIgnoreCase(expectedChecksum.trim());
    }

    /**
     * Runtime exception thrown when document serialization or deserialization fails.
     */
    public static class DocumentSerializationException extends RuntimeException {
        public DocumentSerializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
