package de.joernott.c0de.core.sheet;

/**
 * The dimension type of the c0de dimension as JSON for a data pack
 * ({@code data/c0de/dimension_type/c0de.json}). It is generated, because its height depends on the
 * configuration.
 */
public final class DimensionTypeJson {
    /** Path of the file in a data pack. */
    public static final String PATH = "data/c0de/dimension_type/c0de.json";

    private DimensionTypeJson() {}

    /**
     * The dimension type for a maximum build height. The dimension starts at y=-16; Minecraft requires the
     * bottom and the height to be multiples of 16, so the maximum build height must be 16·k−1.
     */
    public static String create(int maxBuildHeight) {
        int height = height(maxBuildHeight);
        return """
                {
                  "ultrawarm": false,
                  "natural": false,
                  "coordinate_scale": 1.0,
                  "has_skylight": false,
                  "has_ceiling": false,
                  "ambient_light": 0.5,
                  "fixed_time": 6000,
                  "monster_spawn_light_level": 0,
                  "monster_spawn_block_light_limit": 0,
                  "piglin_safe": false,
                  "bed_works": true,
                  "respawn_anchor_works": false,
                  "has_raids": false,
                  "min_y": %d,
                  "height": %d,
                  "logical_height": %d,
                  "infiniburn": "#minecraft:infiniburn_overworld",
                  "effects": "c0de:c0de"
                }
                """.formatted(SheetGeometry.MIN_Y, height, height);
    }

    /**
     * The dimension type in the format of Minecraft 1.21.11 and later: the sky and the fog are environment
     * attributes, the sky box "none" replaces the dimension effects of the mod. The sky shows the fog color, the
     * blue of the biome; there are no clouds, sun, moon and stars.
     */
    public static String createWithAttributes(int maxBuildHeight) {
        int height = height(maxBuildHeight);
        return """
                {
                  "coordinate_scale": 1.0,
                  "has_skylight": false,
                  "has_ceiling": false,
                  "has_fixed_time": true,
                  "ambient_light": 0.5,
                  "monster_spawn_light_level": 0,
                  "monster_spawn_block_light_limit": 0,
                  "min_y": %d,
                  "height": %d,
                  "logical_height": %d,
                  "infiniburn": "#minecraft:infiniburn_overworld",
                  "skybox": "none",
                  "attributes": {
                    "minecraft:visual/sky_color": "%s",
                    "minecraft:visual/fog_color": "%s",
                    "minecraft:gameplay/respawn_anchor_works": false,
                    "minecraft:gameplay/bed_rule": {
                      "can_set_spawn": "always",
                      "can_sleep": "when_dark",
                      "error_message": {"translate": "block.minecraft.bed.no_sleep"}
                    }
                  }
                }
                """.formatted(SheetGeometry.MIN_Y, height, height, SKY_COLOR, SKY_COLOR);
    }

    /** The blue of the sky and the fog, the colors of the biome c0de:greenbar. */
    public static final String SKY_COLOR = "#78a7ff";

    /** Height of the dimension from y=-16 up to and including the maximum build height. */
    public static int height(int maxBuildHeight) {
        int height = maxBuildHeight - SheetGeometry.MIN_Y + 1;
        if (height <= 0 || height % 16 != 0) {
            throw new IllegalArgumentException(
                    "The maximum build height must be 16·k−1 and at least 15: " + maxBuildHeight);
        }
        return height;
    }
}
