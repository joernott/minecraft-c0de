package de.joernott.c0de.core.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * The settings of the mod, stored in the configuration file {@code c0de.properties}.
 *
 * <p>A missing file is created with the default values. Invalid or unknown entries don't stop the
 * mod: they are reported as {@link #problems()} and the default value is used instead.
 */
public final class C0deConfig {
    public static final String FILE_NAME = "c0de.properties";

    /** Highest usable y coordinate of the c0de dimension. It limits the number of PIDs per program. */
    public static final IntSetting MAX_BUILD_HEIGHT = new IntSetting(
            "maxBuildHeight",
            255,
            15,
            2031,
            "Highest usable y coordinate of the c0de dimension (15 to 2031, a multiple of 16 minus 1).",
            "It limits the number of processes per program. Changes apply after a restart.",
            "Lowering it in an existing world removes all blocks above the new height from the c0de dimension.");

    /** Number of slots of the stack of a cursor. */
    public static final IntSetting STACK_SIZE = new IntSetting(
            "stackSize", 2048, 2048, 1_048_576, "Number of slots of the stack of a cursor (2048 to 1048576).");

    /** Time in milliseconds the processes may take per tick; the others continue in the next tick. */
    public static final IntSetting TICK_BUDGET = new IntSetting(
            "tickBudget",
            20,
            1,
            1000,
            "Milliseconds the c0de processes may take per server tick (1 to 1000). If the steps of all processes",
            "take longer, the remaining processes skip the tick and go first in the next one.");

    /** Radius around the trigger block in which players receive SAY messages without a player name. */
    public static final IntSetting SAY_RADIUS = new IntSetting(
            "sayRadius",
            64,
            1,
            1024,
            "Radius in blocks around the trigger block in which players receive messages of SAY (1 to 1024).");

    public static final BooleanSetting ALLOW_INVENTORY_CHANGES = new BooleanSetting(
            "allowInventoryChanges",
            false,
            "Allow players without operator rights to use DELETE_ITEMS of c0de/minecraft.");

    public static final BooleanSetting ALLOW_BLOCK_CHANGES = new BooleanSetting(
            "allowBlockChanges",
            false,
            "Allow players without operator rights to use PLACE_BLOCK (without inventory or with data) and",
            "REMOVE_BLOCK of c0de/minecraft and to open iron doors and iron trapdoors with SET_OPEN.",
            "The data of command blocks, structure blocks and jigsaw blocks always needs operator rights.");

    public static final BooleanSetting ALLOW_SPAWNING = new BooleanSetting(
            "allowSpawning",
            false,
            "Allow players without operator rights to use SPAWN_ITEM and SPAWN_ENTITY of c0de/minecraft.");

    public static final BooleanSetting ALLOW_TELEPORTING = new BooleanSetting(
            "allowTeleporting", false, "Allow players without operator rights to use TELEPORT of c0de/minecraft.");

    /** MOVE_ITEMS of c0de/minecraft; false switches it off for everyone (task 16.12). */
    public static final BooleanSetting ENABLE_MOVE_ITEMS = new BooleanSetting(
            "enableMoveItems",
            true,
            "Allow MOVE_ITEMS of c0de/minecraft, which moves items between inventories that the owner of the",
            "trigger block could open. false switches it off for everyone, operators included.");

    /** The stacks MOVE_ITEMS moves per tick over {@link #MOVE_ITEMS_BLOCKS_PER_TICK} blocks. */
    public static final IntSetting MOVE_ITEMS_STACKS_PER_TICK = new IntSetting(
            "moveItemsStacksPerTick",
            1,
            1,
            1728,
            "Stacks that MOVE_ITEMS moves per tick (1 to 1728). The cursor waits",
            "ceil(stacks / moveItemsStacksPerTick) * ceil(distance / moveItemsBlocksPerTick) ticks.");

    /** The blocks items travel per tick with MOVE_ITEMS. */
    public static final IntSetting MOVE_ITEMS_BLOCKS_PER_TICK = new IntSetting(
            "moveItemsBlocksPerTick",
            128,
            1,
            30_000_000,
            "Blocks that items travel per tick with MOVE_ITEMS (1 to 30000000).");

    /** COMMAND of c0de/minecraft; false switches it off for everyone (task 16.13). */
    public static final BooleanSetting ENABLE_COMMAND = new BooleanSetting(
            "enableCommand",
            true,
            "Allow COMMAND of c0de/minecraft, which runs commands like a command block. It always needs operator",
            "rights and command blocks enabled on the server. false switches it off for everyone.");

    /** All settings in the order they are written to the file. */
    public static final List<Setting<?>> SETTINGS = List.of(
            MAX_BUILD_HEIGHT,
            STACK_SIZE,
            TICK_BUDGET,
            SAY_RADIUS,
            ALLOW_INVENTORY_CHANGES,
            ALLOW_BLOCK_CHANGES,
            ALLOW_SPAWNING,
            ALLOW_TELEPORTING,
            ENABLE_MOVE_ITEMS,
            MOVE_ITEMS_STACKS_PER_TICK,
            MOVE_ITEMS_BLOCKS_PER_TICK,
            ENABLE_COMMAND);

    private final Map<Setting<?>, Object> values = new LinkedHashMap<>();
    private final List<String> problems = new ArrayList<>();

    private C0deConfig() {
        for (Setting<?> setting : SETTINGS) {
            values.put(setting, setting.defaultValue());
        }
    }

    /** A configuration with the default values. */
    public static C0deConfig defaults() {
        return new C0deConfig();
    }

    /**
     * Loads the configuration from {@code directory/c0de.properties}. If the file doesn't exist, it
     * is created with the default values.
     */
    public static C0deConfig load(Path directory) throws IOException {
        Path file = directory.resolve(FILE_NAME);
        C0deConfig config = new C0deConfig();
        if (!Files.exists(file)) {
            Files.createDirectories(directory);
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                config.write(writer);
            }
            return config;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            config.read(reader);
        }
        return config;
    }

    /** Reads the settings in the {@link Properties} format. */
    public void read(Reader reader) throws IOException {
        Properties properties = new Properties();
        properties.load(reader);
        for (String key : properties.stringPropertyNames()) {
            if (SETTINGS.stream().noneMatch(s -> s.key().equals(key))) {
                problems.add("Unknown setting '" + key + "' is ignored.");
            }
        }
        for (Setting<?> setting : SETTINGS) {
            String text = properties.getProperty(setting.key());
            if (text == null) {
                continue;
            }
            try {
                values.put(setting, setting.parse(text.trim()));
            } catch (IllegalArgumentException e) {
                problems.add("Invalid value '" + text.trim() + "' for setting '" + setting.key() + "': "
                        + e.getMessage() + " The default value " + setting.defaultValue() + " is used.");
            }
        }
    }

    /** Writes all settings with their descriptions. */
    public void write(Writer writer) throws IOException {
        writer.write("# Configuration of the c0de mod\n");
        for (Setting<?> setting : SETTINGS) {
            writer.write("\n");
            for (String line : setting.description()) {
                writer.write("# " + line + "\n");
            }
            writer.write("# Default: " + setting.defaultValue() + "\n");
            writer.write(setting.key() + "=" + values.get(setting) + "\n");
        }
    }

    /** The value of a setting. */
    public <T> T get(Setting<T> setting) {
        return setting.type().cast(values.get(setting));
    }

    /** Problems found while reading the file, e.g. invalid values. Empty if the file was valid. */
    public List<String> problems() {
        return List.copyOf(problems);
    }

    /** A setting of the configuration file. */
    public sealed interface Setting<T> permits IntSetting, BooleanSetting {
        String key();

        T defaultValue();

        List<String> description();

        Class<T> type();

        /** Parses the value from the file. Throws {@link IllegalArgumentException} if it is invalid. */
        T parse(String text);
    }

    /** An integer setting with a range. */
    public record IntSetting(String key, Integer defaultValue, int min, int max, List<String> description)
            implements Setting<Integer> {
        IntSetting(String key, int defaultValue, int min, int max, String... description) {
            this(key, defaultValue, min, max, List.of(description));
        }

        @Override
        public Class<Integer> type() {
            return Integer.class;
        }

        @Override
        public Integer parse(String text) {
            int value;
            try {
                value = Integer.parseInt(text);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("It is not an integer.");
            }
            if (value < min || value > max) {
                throw new IllegalArgumentException("It must be between " + min + " and " + max + ".");
            }
            if (this.equals(MAX_BUILD_HEIGHT) && (value + 1) % 16 != 0) {
                throw new IllegalArgumentException("It must be a multiple of 16 minus 1, e.g. 255.");
            }
            return value;
        }
    }

    /** A boolean setting. */
    public record BooleanSetting(String key, Boolean defaultValue, List<String> description)
            implements Setting<Boolean> {
        BooleanSetting(String key, boolean defaultValue, String... description) {
            this(key, defaultValue, List.of(description));
        }

        @Override
        public Class<Boolean> type() {
            return Boolean.class;
        }

        @Override
        public Boolean parse(String text) {
            return switch (text.toLowerCase(java.util.Locale.ROOT)) {
                case "true" -> true;
                case "false" -> false;
                default -> throw new IllegalArgumentException("It must be true or false.");
            };
        }
    }
}
