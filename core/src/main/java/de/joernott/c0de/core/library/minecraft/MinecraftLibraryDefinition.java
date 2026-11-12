package de.joernott.c0de.core.library.minecraft;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.library.Library;
import de.joernott.c0de.core.library.NativeFunction;
import de.joernott.c0de.core.value.BooleanValue;
import de.joernott.c0de.core.value.HashValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.StructDefinition;
import de.joernott.c0de.core.value.StructValue;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * The definitions of the library {@code c0de/minecraft} (docs/concept/12-lib-minecraft.md): its struct types and
 * its functions with their parameters and results (task 15.3). The mod implements the functions; the program
 * runner uses the definitions without the game, so programs that include the library are validated completely and
 * run until they call one of its functions, which then raises INVALID_ARGUMENT.
 */
public final class MinecraftLibraryDefinition {
    public static final String NAME = "c0de/minecraft";

    public static final StructDefinition POSITION = new StructDefinition(
            NAME + "/POSITION",
            List.of(
                    element("x", Type.Simple.INTEGER),
                    element("y", Type.Simple.INTEGER),
                    element("z", Type.Simple.INTEGER),
                    element("dimension", Type.Str.UNLIMITED)));

    public static final StructDefinition ITEM_STACK = new StructDefinition(
            NAME + "/ITEM_STACK", List.of(element("id", Type.Str.UNLIMITED), element("count", Type.Simple.INTEGER)));

    public static final StructDefinition ENTITY = new StructDefinition(
            NAME + "/ENTITY",
            List.of(
                    element("uuid", Type.Str.UNLIMITED),
                    element("type", Type.Str.UNLIMITED),
                    element("name", Type.Str.UNLIMITED),
                    element("position", new Type.Struct(POSITION))));

    private static final Map<String, StructDefinition> TYPES =
            Map.of("POSITION", POSITION, "ITEM_STACK", ITEM_STACK, "ENTITY", ENTITY);

    private MinecraftLibraryDefinition() {}

    private static StructDefinition.Element element(String name, Type type) {
        return new StructDefinition.Element(name, type, Map.of());
    }

    // ---------------------------------------------------------------------------------------- parameters

    private static final Predicate<Value> INTEGER = value -> value instanceof IntegerValue;
    private static final Predicate<Value> BOOLEAN = value -> value instanceof BooleanValue;
    private static final Predicate<Value> TEXT = value -> value instanceof StringValue;
    private static final Predicate<Value> A_POSITION =
            value -> value instanceof StructValue struct && struct.definition().equals(POSITION);
    private static final Predicate<Value> HASH_OF_STRINGS = value -> value instanceof HashValue;

    private static NativeFunction.Parameter position(String name) {
        return NativeFunction.Parameter.required(name, NAME + "/POSITION", A_POSITION);
    }

    private static NativeFunction.Parameter optionalPosition(String name) {
        return NativeFunction.Parameter.optional(name, NAME + "/POSITION", A_POSITION);
    }

    private static NativeFunction.Parameter integer(String name) {
        return NativeFunction.Parameter.required(name, "INTEGER", INTEGER);
    }

    private static NativeFunction.Parameter text(String name) {
        return NativeFunction.Parameter.required(name, "STRING", TEXT);
    }

    private static NativeFunction.Parameter bool(String name) {
        return NativeFunction.Parameter.required(name, "BOOLEAN", BOOLEAN);
    }

    /** A function without its implementation. */
    private record Signature(String name, List<NativeFunction.Parameter> parameters, List<String> results) {}

    private static Signature signature(String name, NativeFunction.Parameter... parameters) {
        // All functions of the library have at most one result, called result; GET_BLOCK has four
        List<String> results = switch (name) {
            case "GET_BLOCK" -> List.of("id", "properties", "power", "has_inventory");
            case "BEEP",
                    "SOUND",
                    "SAY",
                    "SET_LEVER",
                    "PRESS_BUTTON",
                    "SET_OPEN",
                    "PLACE_BLOCK",
                    "REMOVE_BLOCK",
                    "MINE_BLOCK",
                    "TELEPORT" -> List.of();
            default -> List.of("result");
        };
        return new Signature(name, List.of(parameters), results);
    }

    private static final List<Signature> SIGNATURES = List.of(
            signature("TICKS"),
            signature("BEEP"),
            signature(
                    "SOUND",
                    integer("note"),
                    NativeFunction.Parameter.withDefault("instrument", "STRING", TEXT, new StringValue("harp"))),
            signature(
                    "SAY",
                    text("message"),
                    NativeFunction.Parameter.withDefault("player", "STRING", TEXT, new StringValue(""))),
            signature("GET_BLOCK", position("position")),
            signature("GET_INVENTORY", position("position")),
            signature("GET_LEVER", position("position")),
            signature("SET_LEVER", position("position"), bool("on")),
            signature("GET_BUTTON", position("position")),
            signature("PRESS_BUTTON", position("position")),
            signature("GET_PRESSURE_PLATE", position("position")),
            signature("GET_OPEN", position("position")),
            signature("SET_OPEN", position("position"), bool("open")),
            signature("MOVE_ITEMS", position("from"), position("to"), text("id"), integer("count")),
            signature("DELETE_ITEMS", position("position"), text("id"), integer("count")),
            signature(
                    "PLACE_BLOCK",
                    position("position"),
                    text("id"),
                    NativeFunction.Parameter.withDefault(
                            "properties", "HASH STRING", HASH_OF_STRINGS, new HashValue(Type.Str.UNLIMITED)),
                    optionalPosition("inventory")),
            signature(
                    "REMOVE_BLOCK",
                    position("position"),
                    NativeFunction.Parameter.withDefault("drop", "BOOLEAN", BOOLEAN, BooleanValue.TRUE)),
            signature(
                    "MINE_BLOCK",
                    position("position"),
                    position("tool_inventory"),
                    optionalPosition("output_inventory")),
            signature("SPAWN_ITEM", position("position"), text("id"), integer("count")),
            signature("SPAWN_ENTITY", position("position"), text("type")),
            signature(
                    "FIND_ENTITIES",
                    position("position"),
                    integer("radius"),
                    NativeFunction.Parameter.withDefault("type", "STRING", TEXT, new StringValue(""))),
            signature("TELEPORT", text("uuid"), position("position")));

    /**
     * The library with the implementations of the mod by function name. A function without implementation raises
     * INVALID_ARGUMENT, because it needs the game.
     *
     * @throws IllegalArgumentException for an implementation of a function the library doesn't have
     */
    public static Library library(Map<String, NativeFunction.Implementation> implementations) {
        Set<String> unknown = new HashSet<>(implementations.keySet());
        Map<String, NativeFunction> functions = new LinkedHashMap<>();
        for (Signature signature : SIGNATURES) {
            unknown.remove(signature.name());
            NativeFunction.Implementation implementation = implementations.getOrDefault(signature.name(), call -> {
                throw C0deException.of(
                        ErrorCode.INVALID_ARGUMENT,
                        NAME + "/" + signature.name() + " needs the game: it only runs in processes in Minecraft");
            });
            functions.put(
                    signature.name(),
                    new NativeFunction(signature.name(), signature.parameters(), signature.results(), implementation));
        }
        if (!unknown.isEmpty()) {
            throw new IllegalArgumentException(NAME + " has no functions " + unknown);
        }
        Map<String, NativeFunction> all = Map.copyOf(functions);
        return new Library() {
            @Override
            public String name() {
                return NAME;
            }

            @Override
            public Map<String, StructDefinition> types() {
                return TYPES;
            }

            @Override
            public Map<String, NativeFunction> functions() {
                return all;
            }
        };
    }

    /** The library without the game, for the program runner: every function raises INVALID_ARGUMENT. */
    public static Library withoutGame() {
        return library(Map.of());
    }
}
