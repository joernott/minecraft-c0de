package de.joernott.c0de.core.cursor;

import de.joernott.c0de.core.cursor.StackItem.BlockMarker;
import de.joernott.c0de.core.cursor.StackItem.FunctionFrame;
import de.joernott.c0de.core.cursor.StackItem.LoopItem;
import de.joernott.c0de.core.cursor.StackItem.VariableItem;
import de.joernott.c0de.core.eval.TypeRegistry;
import de.joernott.c0de.core.eval.Variable;
import de.joernott.c0de.core.program.Program;
import de.joernott.c0de.core.program.Statement;
import de.joernott.c0de.core.value.ArrayValue;
import de.joernott.c0de.core.value.BooleanValue;
import de.joernott.c0de.core.value.CharValue;
import de.joernott.c0de.core.value.FloatValue;
import de.joernott.c0de.core.value.HashValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.StructDefinition;
import de.joernott.c0de.core.value.StructValue;
import de.joernott.c0de.core.value.TimeValue;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores a cursor as a tree of plain data, so that running processes survive a server restart. The tree
 * consists of {@code Map<String, Object>}, {@code List<Object>}, {@code String}, {@code Long},
 * {@code Double} and {@code Boolean}, without null values; the mod converts it to NBT. Statements are referenced
 * by their line, so restoring needs the same program (sheets can't be changed while processes run).
 */
public final class CursorSnapshot {
    /** Version of the format. */
    public static final long VERSION = 1;

    private CursorSnapshot() {}

    // ------------------------------------------------------------------------------------------- saving

    /** The state of a cursor as a data tree. */
    public static Map<String, Object> save(Cursor cursor) {
        Encoder encoder = new Encoder(cursor.types());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("version", VERSION);
        data.put("entry", cursor.entryFunction());
        data.put("inputs", encoder.values(cursor.inputs()));
        data.put("state", cursor.state().name());
        data.put("line", (long) cursor.line());
        data.put("wait", (long) cursor.waitSteps());
        data.put("exitCode", (long) cursor.exitCode());
        if (cursor.error() != null) {
            data.put("error", encoder.value(cursor.error()));
        }
        data.put("results", encoder.values(cursor.results()));
        data.put("random", cursor.random().state());
        data.put("libraries", new ArrayList<Object>(cursor.libraries()));
        List<Object> types = new ArrayList<>();
        cursor.types().all().forEach((name, definition) -> {
            Map<String, Object> type = new LinkedHashMap<>();
            type.put("name", name);
            type.put("struct", encoder.definition(definition));
            types.add(type);
        });
        data.put("types", types);
        List<Object> functions = new ArrayList<>();
        cursor.functions().values().forEach(function -> {
            if (function instanceof ProgramFunction programFunction) {
                functions.add((long) programFunction.declaration().line());
            }
        });
        data.put("functions", functions);
        List<StackItem> items = cursor.stack().items();
        List<Object> stack = new ArrayList<>();
        for (StackItem item : items) {
            stack.add(encoder.item(item, items));
        }
        data.put("stack", stack);
        return data;
    }

    private record Encoder(TypeRegistry types) {
        Map<String, Object> item(StackItem item, List<StackItem> items) {
            Map<String, Object> data = new LinkedHashMap<>();
            switch (item) {
                case VariableItem variable -> {
                    Variable v = variable.variable();
                    data.put("kind", "variable");
                    data.put("name", v.name());
                    data.put("type", type(v.type()));
                    data.put("value", value(v.value()));
                    data.put("constant", v.constant());
                    data.put("tags", new LinkedHashMap<String, Object>(v.tags()));
                    data.put("line", (long) v.line());
                    data.put("loop", v.loopVariable());
                    data.put("global", variable.global());
                }
                case FunctionFrame frame -> {
                    data.put("kind", "frame");
                    data.put("function", frame.function());
                    data.put("entry", frame.entry());
                    data.put("functionLine", (long) frame.functionLine());
                    data.put(
                            "callLine",
                            frame.call() == null ? -1L : (long) frame.call().line());
                    data.put("returnLine", (long) frame.returnLine());
                    List<Object> results = new ArrayList<>();
                    for (Variable result : frame.results()) {
                        for (int i = 0; i < items.size(); i++) {
                            if (items.get(i) instanceof VariableItem v && v.variable() == result) {
                                results.add((long) i);
                            }
                        }
                    }
                    data.put("results", results);
                }
                case BlockMarker block -> {
                    data.put("kind", "block");
                    data.put("endLine", (long) block.block().endLine());
                }
                case LoopItem loop -> {
                    data.put("kind", "loop");
                    data.put("line", (long) loop.loop().line());
                    if (loop.end() != null) {
                        data.put("end", value(loop.end()));
                        data.put("step", value(loop.step()));
                    }
                    data.put(
                            "remaining",
                            new ArrayList<Object>(
                                    loop.remaining().stream().map(this::value).toList()));
                }
            }
            return data;
        }

        Map<String, Object> values(Map<String, Value> values) {
            Map<String, Object> data = new LinkedHashMap<>();
            values.forEach((name, value) -> data.put(name, value(value)));
            return data;
        }

        Object type(Type type) {
            return switch (type) {
                case Type.Simple simple -> simple.name();
                case Type.Str str -> {
                    Map<String, Object> data = new LinkedHashMap<>();
                    // -1: no length limit
                    data.put("string", str.maxLength() == null ? -1L : str.maxLength());
                    yield data;
                }
                case Type.Array array -> Map.of("array", type(array.element()));
                case Type.Hash hash -> Map.of("hash", type(hash.element()));
                case Type.Struct struct ->
                    struct.definition() == StructDefinition.ERROR
                            ? "ERROR"
                            : Map.of("struct", structName(struct.definition()));
            };
        }

        Map<String, Object> definition(StructDefinition definition) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("name", definition.name());
            List<Object> elements = new ArrayList<>();
            for (StructDefinition.Element element : definition.elements()) {
                Map<String, Object> e = new LinkedHashMap<>();
                e.put("name", element.name());
                e.put("type", type(element.type()));
                e.put("tags", new LinkedHashMap<String, Object>(element.tags()));
                elements.add(e);
            }
            data.put("elements", elements);
            return data;
        }

        private String structName(StructDefinition definition) {
            return types.nameOf(definition)
                    .orElseThrow(() -> new IllegalStateException("Unknown struct " + definition.name()));
        }

        Map<String, Object> value(Value value) {
            Map<String, Object> data = new LinkedHashMap<>();
            switch (value) {
                case BooleanValue b -> put(data, "BOOLEAN", b.value());
                case IntegerValue i -> put(data, "INTEGER", i.value());
                case FloatValue f -> put(data, "FLOAT", f.value());
                case CharValue c -> put(data, "CHAR", (long) c.codePoint());
                case TimeValue t -> put(data, "TIME", t.millis());
                case StringValue s -> put(data, "STRING", s.value());
                case ArrayValue array -> {
                    put(
                            data,
                            "ARRAY",
                            new ArrayList<Object>(
                                    array.elements().stream().map(this::value).toList()));
                    data.put("element", type(array.elementType()));
                }
                case HashValue hash -> {
                    put(data, "HASH", values(hash.elements()));
                    data.put("element", type(hash.elementType()));
                }
                case StructValue struct -> {
                    Map<String, Object> elements = new LinkedHashMap<>();
                    for (StructDefinition.Element element : struct.definition().elements()) {
                        elements.put(element.name(), value(struct.get(element.name())));
                    }
                    put(data, "STRUCT", elements);
                    data.put(
                            "struct",
                            struct.definition() == StructDefinition.ERROR ? "ERROR" : structName(struct.definition()));
                }
            }
            return data;
        }

        private static void put(Map<String, Object> data, String type, Object value) {
            data.put("type", type);
            data.put("value", value);
        }
    }

    // ---------------------------------------------------------------------------------------- restoring

    /**
     * Restores a cursor.
     *
     * @param data the tree created by {@link #save}
     * @param program the program the cursor runs (parsed from the same sheet)
     * @param settings the environment
     * @throws IllegalArgumentException if the data doesn't fit the program or has an unknown version
     */
    public static Cursor restore(Map<String, Object> data, Program program, CursorSettings settings) {
        if (number(data.get("version")) != VERSION) {
            throw new IllegalArgumentException("Unknown version of the cursor data: " + data.get("version"));
        }
        TypeRegistry types = new TypeRegistry();
        Decoder decoder = new Decoder(types);
        // Library types must be known before the inputs are decoded
        for (Object entry : list(data.get("types"))) {
            Map<String, Object> type = map(entry);
            types.add((String) type.get("name"), decoder.definition(map(type.get("struct"))));
        }
        Cursor cursor =
                new Cursor(program, (String) data.get("entry"), decoder.values(map(data.get("inputs"))), settings);
        types.all().forEach((name, definition) -> cursor.types().add(name, definition));
        for (Object library : list(data.get("libraries"))) {
            cursor.restoreLibrary((String) library);
        }
        ProgramIndex index = cursor.index();
        for (Object line : list(data.get("functions"))) {
            if (!(index.statement((int) number(line)) instanceof Statement.FunctionDeclaration declaration)) {
                throw new IllegalArgumentException("There is no function in line " + line);
            }
            cursor.restoreFunction(declaration);
        }
        List<StackItem> items = cursor.stack().items();
        List<Map<String, Object>> frames = new ArrayList<>();
        for (Object entry : list(data.get("stack"))) {
            Map<String, Object> item = map(entry);
            switch ((String) item.get("kind")) {
                case "variable" ->
                    items.add(new VariableItem(
                            new Variable(
                                    (String) item.get("name"),
                                    decoder.type(item.get("type")),
                                    decoder.value(map(item.get("value"))),
                                    (Boolean) item.get("constant"),
                                    strings(map(item.get("tags"))),
                                    (int) number(item.get("line")),
                                    (Boolean) item.get("loop")),
                            (Boolean) item.get("global")));
                case "frame" -> {
                    int callLine = (int) number(item.get("callLine"));
                    Statement.Call call = null;
                    if (callLine >= 0) {
                        if (!(index.statement(callLine) instanceof Statement.Call c)) {
                            throw new IllegalArgumentException("There is no CALL statement in line " + callLine);
                        }
                        call = c;
                    }
                    items.add(new FunctionFrame(
                            (String) item.get("function"),
                            (Boolean) item.get("entry"),
                            (int) number(item.get("functionLine")),
                            call,
                            (int) number(item.get("returnLine")),
                            List.of()));
                    frames.add(item);
                }
                case "block" -> {
                    int endLine = (int) number(item.get("endLine"));
                    ProgramIndex.EndNode end = index.end(endLine);
                    if (end == null) {
                        throw new IllegalArgumentException("There is no END line in line " + endLine);
                    }
                    items.add(new BlockMarker(end.owner(), end.block()));
                }
                case "loop" -> {
                    int line = (int) number(item.get("line"));
                    Statement loop = index.statement(line);
                    if (loop == null) {
                        throw new IllegalArgumentException("There is no loop in line " + line);
                    }
                    List<Value> remaining = new ArrayList<>();
                    for (Object value : list(item.get("remaining"))) {
                        remaining.add(decoder.value(map(value)));
                    }
                    items.add(new LoopItem(
                            loop,
                            item.get("end") == null ? null : decoder.value(map(item.get("end"))),
                            item.get("step") == null ? null : decoder.value(map(item.get("step"))),
                            remaining));
                }
                default -> throw new IllegalArgumentException("Unknown stack item " + item.get("kind"));
            }
        }
        // Link the result variables of the function items
        int frameIndex = 0;
        for (StackItem item : items) {
            if (item instanceof FunctionFrame frame) {
                for (Object position : list(frames.get(frameIndex++).get("results"))) {
                    frame.results().add(((VariableItem) items.get((int) number(position))).variable());
                }
            }
        }
        Map<String, Value> results = decoder.values(map(data.get("results")));
        cursor.restore(
                CursorState.valueOf((String) data.get("state")),
                (int) number(data.get("line")),
                (int) number(data.get("wait")),
                (int) number(data.get("exitCode")),
                data.get("error") == null ? null : (StructValue) decoder.value(map(data.get("error"))),
                results,
                number(data.get("random")));
        return cursor;
    }

    private record Decoder(TypeRegistry types) {
        StructDefinition definition(Map<String, Object> data) {
            List<StructDefinition.Element> elements = new ArrayList<>();
            for (Object entry : list(data.get("elements"))) {
                Map<String, Object> element = map(entry);
                elements.add(new StructDefinition.Element(
                        (String) element.get("name"), type(element.get("type")), strings(map(element.get("tags")))));
            }
            return new StructDefinition((String) data.get("name"), elements);
        }

        Type type(Object data) {
            if (data instanceof String name) {
                return name.equals("ERROR") ? Type.Struct.ERROR : Type.Simple.valueOf(name);
            }
            Map<String, Object> type = map(data);
            if (type.containsKey("string")) {
                long max = number(type.get("string"));
                return new Type.Str(max < 0 ? null : max);
            }
            if (type.containsKey("array")) {
                return new Type.Array(type(type.get("array")));
            }
            if (type.containsKey("hash")) {
                return new Type.Hash(type(type.get("hash")));
            }
            return new Type.Struct(struct((String) type.get("struct")));
        }

        private StructDefinition struct(String name) {
            return name.equals("ERROR")
                    ? StructDefinition.ERROR
                    : types.find(name).orElseThrow(() -> new IllegalArgumentException("Unknown struct " + name));
        }

        Map<String, Value> values(Map<String, Object> data) {
            Map<String, Value> values = new LinkedHashMap<>();
            data.forEach((name, value) -> values.put(name, value(map(value))));
            return values;
        }

        Value value(Map<String, Object> data) {
            Object value = data.get("value");
            return switch ((String) data.get("type")) {
                case "BOOLEAN" -> BooleanValue.of((Boolean) value);
                case "INTEGER" -> new IntegerValue(number(value));
                case "FLOAT" -> new FloatValue(((Number) value).doubleValue());
                case "CHAR" -> new CharValue((int) number(value));
                case "TIME" -> new TimeValue(number(value));
                case "STRING" -> new StringValue((String) value);
                case "ARRAY" -> {
                    List<Value> elements = new ArrayList<>();
                    for (Object element : list(value)) {
                        elements.add(value(map(element)));
                    }
                    yield new ArrayValue(type(data.get("element")), elements);
                }
                case "HASH" -> new HashValue(type(data.get("element")), values(map(value)));
                case "STRUCT" -> {
                    StructValue struct = new StructValue(struct((String) data.get("struct")));
                    map(value).forEach((element, elementValue) -> struct.set(element, value(map(elementValue))));
                    yield struct;
                }
                default -> throw new IllegalArgumentException("Unknown value type " + data.get("type"));
            };
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object data) {
        return (Map<String, Object>) data;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> list(Object data) {
        return (List<Object>) data;
    }

    private static long number(Object data) {
        return ((Number) data).longValue();
    }

    private static Map<String, String> strings(Map<String, Object> data) {
        Map<String, String> result = new LinkedHashMap<>();
        data.forEach((key, value) -> result.put(key, (String) value));
        return result;
    }
}
