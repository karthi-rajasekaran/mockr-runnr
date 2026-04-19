package com.mockr.runnr.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.UUID;

/**
 * Custom JPA AttributeConverter for UUID <-> String conversion.
 * 
 * Solves SQLite + Hibernate 6 UUID mapping issue where SQLite JDBC driver
 * doesn't properly handle UUID types. This converter explicitly converts
 * UUID to/from String representation stored in SQLite TEXT column.
 * 
 * Why this is needed:
 * - SQLite stores UUID as TEXT (string representation)
 * - Hibernate's default UUID handling expects binary storage
 * - SQLite JDBC driver cannot convert TEXT to UUID without explicit guidance
 * - This converter provides explicit bidirectional conversion
 * 
 * Scope: Applied to all entity UUID fields
 * Usage: @Convert(converter = UUIDConverter.class) on UUID fields
 */
@Converter(autoApply = true)
public class UUIDConverter implements AttributeConverter<UUID, String> {

    /**
     * Convert UUID to String for database storage.
     * 
     * @param uuid UUID entity attribute (null-safe)
     * @return String representation (UUID.toString()) or null if input is null
     */
    @Override
    public String convertToDatabaseColumn(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return uuid.toString();
    }

    /**
     * Convert String to UUID from database retrieval.
     * 
     * @param dbData String from database (null-safe, trimmed)
     * @return Parsed UUID or null if input is null
     * @throws IllegalArgumentException if string is not a valid UUID format
     */
    @Override
    public UUID convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(dbData.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Failed to parse UUID from database value: " + dbData, e);
        }
    }
}
