package com.mockr.runnr.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import jakarta.persistence.Converter;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UUIDConverter - SQLite UUID <-> String conversion")
class UUIDConverterTest {

    private final UUIDConverter converter = new UUIDConverter();

    @Test
    @DisplayName("Should convert UUID to String for database storage")
    void shouldConvertUuidToDatabaseColumn() {
        UUID uuid = UUID.fromString("015f88c8-a7fb-4025-8558-df6ac951b1c8");

        String result = converter.convertToDatabaseColumn(uuid);

        assertEquals("015f88c8-a7fb-4025-8558-df6ac951b1c8", result);
    }

    @Test
    @DisplayName("Should convert null UUID to null String")
    void shouldHandleNullUuidToDatabaseColumn() {
        String result = converter.convertToDatabaseColumn(null);

        assertNull(result);
    }

    @Test
    @DisplayName("Should convert String to UUID from database")
    void shouldConvertEntityAttribute() {
        String dbData = "015f88c8-a7fb-4025-8558-df6ac951b1c8";

        UUID result = converter.convertToEntityAttribute(dbData);

        assertEquals(UUID.fromString("015f88c8-a7fb-4025-8558-df6ac951b1c8"), result);
    }

    @Test
    @DisplayName("Should handle null String from database")
    void shouldHandleNullEntityAttribute() {
        UUID result = converter.convertToEntityAttribute(null);

        assertNull(result);
    }

    @Test
    @DisplayName("Should handle empty String from database")
    void shouldHandleEmptyEntityAttribute() {
        UUID result = converter.convertToEntityAttribute("");

        assertNull(result);
    }

    @Test
    @DisplayName("Should handle whitespace String from database")
    void shouldHandleWhitespaceEntityAttribute() {
        UUID result = converter.convertToEntityAttribute("   ");

        assertNull(result);
    }

    @Test
    @DisplayName("Should trim whitespace from database String")
    void shouldTrimWhitespaceFromDbData() {
        String dbData = "  015f88c8-a7fb-4025-8558-df6ac951b1c8  ";

        UUID result = converter.convertToEntityAttribute(dbData);

        assertEquals(UUID.fromString("015f88c8-a7fb-4025-8558-df6ac951b1c8"), result);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid UUID")
    void shouldThrowForInvalidUuid() {
        String dbData = "invalid-uuid-string";

        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute(dbData));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for malformed UUID")
    void shouldThrowForMalformedUuid() {
        String dbData = "015f88c8-a7fb-4025-8558"; // incomplete UUID

        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute(dbData));
    }

    @Test
    @DisplayName("Should be bidirectional - convert to DB and back")
    void shouldBeBidirectional() {
        UUID original = UUID.randomUUID();

        String dbFormat = converter.convertToDatabaseColumn(original);
        UUID restored = converter.convertToEntityAttribute(dbFormat);

        assertEquals(original, restored);
    }

    @Test
    @DisplayName("Should handle multiple random UUIDs")
    void shouldHandleMultipleRandomUuids() {
        for (int i = 0; i < 100; i++) {
            UUID original = UUID.randomUUID();

            String dbFormat = converter.convertToDatabaseColumn(original);
            UUID restored = converter.convertToEntityAttribute(dbFormat);

            assertEquals(original, restored);
        }
    }

    @Test
    @DisplayName("Should preserve UUID format exactly")
    void shouldPreserveUuidFormatExactly() {
        UUID uuid = UUID.fromString("12345678-1234-5678-1234-567812345678");

        String dbFormat = converter.convertToDatabaseColumn(uuid);

        assertEquals("12345678-1234-5678-1234-567812345678", dbFormat);
    }

    @Test
    @DisplayName("Should handle UUID with leading zeros")
    void shouldHandleUuidWithLeadingZeros() {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        String dbFormat = converter.convertToDatabaseColumn(uuid);
        UUID restored = converter.convertToEntityAttribute(dbFormat);

        assertEquals(uuid, restored);
    }

    @Test
    @DisplayName("Should be marked as AutoApply for global use")
    void shouldBeMarkedAsAutoApply() {
        Converter converterAnnotation = UUIDConverter.class.getAnnotation(Converter.class);

        assertNotNull(converterAnnotation);
        assertTrue(converterAnnotation.autoApply());
    }
}
