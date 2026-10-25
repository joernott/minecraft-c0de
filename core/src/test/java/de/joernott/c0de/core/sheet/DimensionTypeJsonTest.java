package de.joernott.c0de.core.sheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DimensionTypeJsonTest {
    @Test
    void heightIncludesTheFloorAndTheMaximumBuildHeight() {
        // y=-16 to y=255
        assertEquals(272, DimensionTypeJson.height(255));
        assertEquals(32, DimensionTypeJson.height(15));
        assertEquals(2048, DimensionTypeJson.height(2031));
    }

    @Test
    void invalidMaximumBuildHeightsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> DimensionTypeJson.height(16));
        assertThrows(IllegalArgumentException.class, () -> DimensionTypeJson.height(-17));
    }

    @Test
    void jsonContainsTheHeightAndTheProperties() {
        String json = DimensionTypeJson.create(511);
        assertTrue(json.contains("\"min_y\": -16,"), json);
        assertTrue(json.contains("\"height\": 528,"), json);
        assertTrue(json.contains("\"logical_height\": 528,"), json);
        assertTrue(json.contains("\"has_skylight\": false"), json);
        assertTrue(json.contains("\"fixed_time\": 6000"), json);
        assertTrue(json.contains("\"effects\": \"c0de:c0de\""), json);
    }

    @Test
    void jsonWithAttributesHasNoSky() {
        String json = DimensionTypeJson.createWithAttributes(511);
        assertTrue(json.contains("\"height\": 528,"), json);
        assertTrue(json.contains("\"skybox\": \"none\""), json);
        assertTrue(json.contains("\"minecraft:visual/fog_color\": \"#78a7ff\""), json);
    }

    @Test
    void ambientLightColorOnlyWhenRequested() {
        String withColor = DimensionTypeJson.createWithAttributes(511, true);
        assertTrue(withColor.contains("\"minecraft:visual/ambient_light_color\": \"#808080\","), withColor);
        String without = DimensionTypeJson.createWithAttributes(511);
        assertTrue(!without.contains("ambient_light_color"), without);
    }
}
