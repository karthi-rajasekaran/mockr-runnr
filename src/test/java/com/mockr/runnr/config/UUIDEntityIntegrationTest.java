package com.mockr.runnr.config;

import com.mockr.runnr.domain.Project;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UUID Entity Integration - Simulates Hibernate persistence")
class UUIDEntityIntegrationTest {

    private final UUIDConverter converter = new UUIDConverter();

    @Test
    @DisplayName("Should simulate Project entity UUID persistence flow")
    void shouldSimulateProjectEntityPersistence() {
        // Create Project entity with UUID
        Project project = new Project();
        UUID generatedId = UUID.randomUUID();
        project.setId(generatedId);
        project.setName("Test Project");
        project.setContextPath("/api/test");

        // Simulate Hibernate INSERT: convert UUID to database format
        String persistedId = converter.convertToDatabaseColumn(project.getId());
        assertNotNull(persistedId);
        assertTrue(persistedId.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));

        // Simulate Hibernate SELECT: convert database format back to UUID
        UUID retrievedId = converter.convertToEntityAttribute(persistedId);
        assertEquals(generatedId, retrievedId);
        assertEquals(project.getId(), retrievedId);
    }

    @Test
    @DisplayName("Should handle realistic database string format")
    void shouldHandleRealisticDatabaseFormat() {
        // This is what SQLite stores in TEXT column
        String sqliteTextValue = "015f88c8-a7fb-4025-8558-df6ac951b1c8";

        // Hibernate reads TEXT and converts to UUID
        UUID uuid = converter.convertToEntityAttribute(sqliteTextValue);

        assertNotNull(uuid);
        assertEquals("015f88c8-a7fb-4025-8558-df6ac951b1c8", uuid.toString());
    }

    @Test
    @DisplayName("Should handle multiple UUID fields in entity")
    void shouldHandleMultipleUuidFields() {
        // Simulate multiple UUID fields (like in Endpoint, Response, Condition
        // entities)
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();

        // All convert correctly
        String db1 = converter.convertToDatabaseColumn(id1);
        String db2 = converter.convertToDatabaseColumn(id2);
        String db3 = converter.convertToDatabaseColumn(id3);

        UUID restored1 = converter.convertToEntityAttribute(db1);
        UUID restored2 = converter.convertToEntityAttribute(db2);
        UUID restored3 = converter.convertToEntityAttribute(db3);

        assertEquals(id1, restored1);
        assertEquals(id2, restored2);
        assertEquals(id3, restored3);

        // All are different
        assertNotEquals(restored1, restored2);
        assertNotEquals(restored2, restored3);
        assertNotEquals(restored1, restored3);
    }

    @Test
    @DisplayName("Should handle edge case: nil UUID")
    void shouldHandleNilUuid() {
        UUID nilUuid = new UUID(0L, 0L); // 00000000-0000-0000-0000-000000000000

        String persisted = converter.convertToDatabaseColumn(nilUuid);
        UUID restored = converter.convertToEntityAttribute(persisted);

        assertEquals(nilUuid, restored);
        assertEquals("00000000-0000-0000-0000-000000000000", restored.toString());
    }

    @Test
    @DisplayName("Should handle edge case: max UUID")
    void shouldHandleMaxUuid() {
        UUID maxUuid = new UUID(Long.MAX_VALUE, Long.MAX_VALUE); // ffffffff-ffff-ffff-ffff-ffffffffffff

        String persisted = converter.convertToDatabaseColumn(maxUuid);
        UUID restored = converter.convertToEntityAttribute(persisted);

        assertEquals(maxUuid, restored);
    }

    @Test
    @DisplayName("Should handle whitespace from database")
    void shouldHandleWhitespaceInDatabase() {
        // SQLite sometimes returns strings with surrounding whitespace
        String dbValueWithWhitespace = "  " + UUID.randomUUID().toString() + "  ";

        UUID uuid = converter.convertToEntityAttribute(dbValueWithWhitespace);

        assertNotNull(uuid);
    }
}
