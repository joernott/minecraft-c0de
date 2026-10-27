package de.joernott.c0de.core.library.standard;

import de.joernott.c0de.core.value.BooleanValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.StructDefinition;
import de.joernott.c0de.core.value.StructValue;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The built-in struct types and constants of the language (docs/concept/08-builtin-functions.md), like the names of
 * the error codes: SHEET and PID, which describe the sheet and the process a program runs in (tasks 13.4, 13.5), and
 * the names of the colors of COLOR (task 13.6). Without a process, e.g. during validation, SHEET and PID contain
 * default values; the mod passes the real ones in the settings of the cursor.
 */
public final class BuiltinConstants {
    /** The type of SHEET. */
    public static final StructDefinition SHEET_INFO = new StructDefinition(
            "SHEET_INFO",
            List.of(
                    element("number", Type.Simple.INTEGER),
                    element("program", Type.Str.UNLIMITED),
                    element("owner", Type.Str.UNLIMITED),
                    element("x", Type.Simple.INTEGER),
                    element("y", Type.Simple.INTEGER),
                    element("z", Type.Simple.INTEGER)));

    /** The type of PID. */
    public static final StructDefinition PID_INFO = new StructDefinition(
            "PID_INFO",
            List.of(
                    element("pid", Type.Simple.INTEGER),
                    element("program", Type.Str.UNLIMITED),
                    element("function", Type.Str.UNLIMITED),
                    element("owner", Type.Str.UNLIMITED),
                    element("activator", Type.Str.UNLIMITED),
                    element("x", Type.Simple.INTEGER),
                    element("y", Type.Simple.INTEGER),
                    element("z", Type.Simple.INTEGER),
                    element("dimension", Type.Str.UNLIMITED),
                    element("debug", Type.Simple.BOOLEAN),
                    element("started", Type.Simple.INTEGER)));

    /** The 16 colors of Minecraft text in the order of their numbers 0 to 15, without the prefix COLOR_. */
    public static final List<String> COLORS = List.of(
            "BLACK",
            "DARK_BLUE",
            "DARK_GREEN",
            "DARK_AQUA",
            "DARK_RED",
            "DARK_PURPLE",
            "GOLD",
            "GRAY",
            "DARK_GRAY",
            "BLUE",
            "GREEN",
            "AQUA",
            "RED",
            "LIGHT_PURPLE",
            "YELLOW",
            "WHITE");

    /** The built-in struct types, in the order the label dialog offers them. */
    private static final Map<String, StructDefinition> TYPES = structs();

    private static final Map<String, Value> CONSTANTS = constants();

    private BuiltinConstants() {}

    /** The built-in struct types by name. */
    public static Map<String, StructDefinition> types() {
        return TYPES;
    }

    /** The built-in constants with their default values: SHEET, PID and COLOR_BLACK to COLOR_WHITE. */
    public static Map<String, Value> defaults() {
        return CONSTANTS;
    }

    private static Map<String, StructDefinition> structs() {
        Map<String, StructDefinition> types = new LinkedHashMap<>();
        types.put("SHEET_INFO", SHEET_INFO);
        types.put("PID_INFO", PID_INFO);
        return Collections.unmodifiableMap(types);
    }

    private static Map<String, Value> constants() {
        Map<String, Value> constants = new LinkedHashMap<>();
        constants.put("SHEET", new StructValue(SHEET_INFO));
        constants.put("PID", new StructValue(PID_INFO));
        for (int i = 0; i < COLORS.size(); i++) {
            constants.put("COLOR_" + COLORS.get(i), new IntegerValue(i));
        }
        return Collections.unmodifiableMap(constants);
    }

    /**
     * The value of SHEET.
     *
     * @param x the position of the first block of the sheet floor (relative x and z 0)
     */
    public static StructValue sheet(long number, String program, String owner, long x, long y, long z) {
        StructValue sheet = new StructValue(SHEET_INFO);
        sheet.set("number", new IntegerValue(number));
        sheet.set("program", new StringValue(program));
        sheet.set("owner", new StringValue(owner));
        sheet.set("x", new IntegerValue(x));
        sheet.set("y", new IntegerValue(y));
        sheet.set("z", new IntegerValue(z));
        return sheet;
    }

    /**
     * The value of PID.
     *
     * @param activator the name of the player who started the process, or the empty string
     * @param x the position of the trigger block
     * @param dimension the dimension of the trigger block
     * @param started the game time of the dimension of the trigger block when the process started
     */
    public static StructValue pid(
            long pid,
            String program,
            String function,
            String owner,
            String activator,
            long x,
            long y,
            long z,
            String dimension,
            boolean debug,
            long started) {
        StructValue value = new StructValue(PID_INFO);
        value.set("pid", new IntegerValue(pid));
        value.set("program", new StringValue(program));
        value.set("function", new StringValue(function));
        value.set("owner", new StringValue(owner));
        value.set("activator", new StringValue(activator));
        value.set("x", new IntegerValue(x));
        value.set("y", new IntegerValue(y));
        value.set("z", new IntegerValue(z));
        value.set("dimension", new StringValue(dimension));
        value.set("debug", BooleanValue.of(debug));
        value.set("started", new IntegerValue(started));
        return value;
    }

    private static StructDefinition.Element element(String name, Type type) {
        return new StructDefinition.Element(name, type, Map.of());
    }
}
