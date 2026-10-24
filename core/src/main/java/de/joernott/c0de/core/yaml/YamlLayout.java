package de.joernott.c0de.core.yaml;

import de.joernott.c0de.core.line.Notation;
import de.joernott.c0de.core.line.Sheet;
import de.joernott.c0de.core.program.TypeSpec;
import de.joernott.c0de.core.program.TypeText;
import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.core.value.BooleanValue;
import de.joernott.c0de.core.value.CharValue;
import de.joernott.c0de.core.value.FloatValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.Literals;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.TimeValue;
import de.joernott.c0de.core.value.Type;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lays out a program read from YAML on a sheet (docs/concept/17-yaml-format.md): computes the indentation,
 * inserts the BEGIN and END lines and continues long expressions on continuation lines. The result is the
 * notation of the sheet, parsed into the line model.
 */
final class YamlLayout {
    /** The number of blocks between x=5 and x=28. */
    private static final int WIDTH = SheetGeometry.CODE_MAX_X - SheetGeometry.CODE_MIN_X + 1;

    private final List<String> lines = new ArrayList<>();

    /** The element types of the structs declared in the program, for the values of struct initializers. */
    private final Map<String, Map<String, TypeSpec>> structs = new HashMap<>();

    private YamlLayout() {}

    /**
     * Lays out a program.
     *
     * @throws IllegalArgumentException if the document is not a program or a line doesn't fit on the sheet
     */
    static Sheet layout(Map<?, ?> root) {
        YamlLayout layout = new YamlLayout();
        layout.program(root);
        return Notation.parse(String.join("\n", layout.lines) + "\n", ZoneOffset.UTC);
    }

    private void program(Map<?, ?> root) {
        line(0, List.of("PROGRAM", name(root.get("PROGRAM"), "PROGRAM")));
        line(0, List.of("NOOP"));
        if (root.get("INCLUDE") instanceof List<?> includes) {
            for (Object include : includes) {
                line(0, List.of("INCLUDE", name(include, "INCLUDE")));
            }
        }
        List<?> code = list(root.get("code"));
        // The struct types are needed for the values of initializers that may come before their use
        for (Object entry : code) {
            if (entry instanceof Map<?, ?> map && "STRUCT".equals(map.get("keyword"))) {
                Map<String, TypeSpec> elements = new HashMap<>();
                for (Object element : list(map.get("code"))) {
                    Map<?, ?> item = map(element);
                    elements.put(name(item.get("name"), "name"), type(item));
                }
                structs.put(name(map.get("name"), "name"), elements);
            }
        }
        statements(code, 0);
    }

    // ------------------------------------------------------------------------------------------------- lines

    /** Adds a line; a line that doesn't fit on the sheet makes the import fail. */
    private void line(int indent, List<String> tokens) {
        if (indent + tokens.size() > WIDTH) {
            throw new IllegalArgumentException(
                    "Line " + lines.size() + " doesn't fit on the sheet: " + String.join(" ", tokens));
        }
        lines.add("    ".repeat(indent) + String.join(" ", tokens));
    }

    /**
     * Adds a line with an expression at its end, followed by {@code ;} and the comment of the line. The parts
     * of the expression that don't fit are written on continuation lines, indented by one block.
     */
    private void expressionLine(int indent, List<String> prefix, String expression, List<String> comment) {
        List<String> tokens = new ArrayList<>(prefix);
        tokens.addAll(ExpressionText.split(expression));
        tokens.add(";");
        int first = WIDTH - indent - comment.size();
        if (tokens.size() <= first) {
            List<String> all = new ArrayList<>(tokens);
            all.addAll(comment);
            line(indent, all);
            return;
        }
        if (first <= prefix.size()) {
            throw new IllegalArgumentException(
                    "Line " + lines.size() + " doesn't fit on the sheet: " + String.join(" ", prefix));
        }
        List<String> head = new ArrayList<>(tokens.subList(0, first));
        head.addAll(comment);
        line(indent, head);
        int width = WIDTH - indent - 1;
        for (int start = first; start < tokens.size(); start += width) {
            line(indent + 1, tokens.subList(start, Math.min(tokens.size(), start + width)));
        }
    }

    /** The COMMENT block of an object and the blocks after it, as tokens. */
    private static List<String> comment(Map<?, ?> entry) {
        List<String> tokens = new ArrayList<>();
        if (entry.get("comment") != null) {
            tokens.add("COMMENT(" + quote(String.valueOf(entry.get("comment"))) + ")");
            if (entry.get("disabled") != null) {
                tokens.addAll(ExpressionText.split(String.valueOf(entry.get("disabled"))));
            }
        }
        return tokens;
    }

    private static List<String> with(List<String> tokens, List<String> more) {
        List<String> all = new ArrayList<>(tokens);
        all.addAll(more);
        return all;
    }

    // -------------------------------------------------------------------------------------------- statements

    private void statements(List<?> entries, int indent) {
        for (Object item : entries) {
            statement(map(item), indent);
        }
    }

    private void block(Map<?, ?> entry, String key, int indent) {
        line(indent, List.of("BEGIN"));
        statements(list(entry.get(key)), indent + 1);
    }

    private void statement(Map<?, ?> entry, int indent) {
        String keyword = name(entry.get("keyword"), "keyword");
        List<String> comment = comment(entry);
        switch (keyword) {
            case "NOOP" -> line(indent, List.of("NOOP"));
            case "COMMENT" -> {
                List<String> tokens = new ArrayList<>();
                tokens.add("COMMENT(" + quote(String.valueOf(entry.get("text"))) + ")");
                if (entry.get("disabled") != null) {
                    tokens.addAll(ExpressionText.split(String.valueOf(entry.get("disabled"))));
                }
                line(indent, tokens);
            }
            case "STRUCT" -> {
                line(indent, with(List.of("STRUCT", name(entry.get("name"), "name")), comment));
                line(indent, List.of("BEGIN"));
                for (Object element : list(entry.get("code"))) {
                    Map<?, ?> item = map(element);
                    List<String> tokens = new ArrayList<>();
                    tokens.add(name(item.get("name"), "name"));
                    tokens.addAll(typeTokens(item));
                    tokens.addAll(tagTokens(item));
                    line(indent + 1, with(tokens, comment(item)));
                }
                line(indent, List.of("END", "STRUCT"));
            }
            case "VARIABLE", "CONSTANT" -> declaration(entry, keyword, indent, comment);
            case "FUNCTION" -> {
                line(indent, with(List.of("FUNCTION", name(entry.get("name"), "name")), comment));
                for (String kind : List.of("parameters", "results")) {
                    for (Object item : list(entry.get(kind))) {
                        Map<?, ?> member = map(item);
                        List<String> tokens = new ArrayList<>();
                        tokens.add(kind.equals("parameters") ? "PARAMETER" : "RESULT");
                        tokens.add(name(member.get("name"), "name"));
                        tokens.addAll(typeTokens(member));
                        if (member.get("value") != null) {
                            tokens.add(literal(member.get("value"), type(member)));
                        }
                        line(indent + 1, with(tokens, comment(member)));
                    }
                }
                block(entry, "code", indent);
                line(indent, List.of("END", "FUNCTION"));
            }
            case "ASSIGN" ->
                expressionLine(
                        indent,
                        List.of("ASSIGN", name(entry.get("name"), "name")),
                        text(entry.get("expression"), "expression"),
                        comment);
            case "CALL" -> {
                List<String> tokens = new ArrayList<>(List.of("CALL", name(entry.get("name"), "name")));
                if (entry.get("error") != null) {
                    tokens.add(String.valueOf(entry.get("error")));
                }
                line(indent, with(tokens, comment));
                for (Object item : list(entry.get("parameters"))) {
                    Map<?, ?> parameter = map(item);
                    expressionLine(
                            indent + 1,
                            List.of("PARAMETER", name(parameter.get("name"), "name")),
                            text(parameter.get("expression"), "expression"),
                            comment(parameter));
                }
                for (Object item : list(entry.get("results"))) {
                    Map<?, ?> result = map(item);
                    line(
                            indent + 1,
                            with(
                                    List.of(
                                            "RESULT",
                                            name(result.get("name"), "name"),
                                            text(result.get("target"), "target")),
                                    comment(result)));
                }
            }
            case "IF" -> {
                expressionLine(indent, List.of("IF"), text(entry.get("expression"), "expression"), comment);
                block(entry, "code", indent);
                if (entry.get("else") != null) {
                    line(indent, List.of("END"));
                    line(indent, List.of("ELSE"));
                    block(entry, "else", indent);
                }
                line(indent, List.of("END", "IF"));
            }
            case "FOR" -> {
                // FOR i INTEGER from ; to ;  or  FOR i INTEGER from ; to ; step ;
                List<String> tokens = new ArrayList<>(
                        List.of("FOR", name(entry.get("name"), "name"), name(entry.get("type"), "type")));
                tokens.addAll(ExpressionText.split(text(entry.get("from"), "from")));
                tokens.add(";");
                String last = text(entry.get("to"), "to");
                if (entry.get("step") != null) {
                    tokens.addAll(ExpressionText.split(last));
                    tokens.add(";");
                    last = text(entry.get("step"), "step");
                }
                expressionLine(indent, tokens, last, comment);
                block(entry, "code", indent);
                line(indent, List.of("END", "FOR"));
            }
            case "EACH" -> {
                line(
                        indent,
                        with(
                                List.of(
                                        "EACH",
                                        name(entry.get("name"), "name"),
                                        text(entry.get("iterable"), "iterable")),
                                comment));
                block(entry, "code", indent);
                line(indent, List.of("END", "EACH"));
            }
            case "WHILE" -> {
                expressionLine(indent, List.of("WHILE"), text(entry.get("expression"), "expression"), comment);
                block(entry, "code", indent);
                line(indent, List.of("END", "WHILE"));
            }
            case "REPEAT" -> {
                line(indent, with(List.of("REPEAT"), comment));
                block(entry, "code", indent);
                expressionLine(
                        indent, List.of("END", "REPEAT"), text(entry.get("expression"), "expression"), List.of());
            }
            case "BREAK" -> {
                if (entry.get("expression") == null) {
                    line(indent, with(List.of("BREAK"), comment));
                } else {
                    expressionLine(indent, List.of("BREAK"), text(entry.get("expression"), "expression"), comment);
                }
            }
            case "RETURN" -> {
                List<String> tokens = new ArrayList<>(List.of("RETURN"));
                if (entry.get("name") != null) {
                    tokens.add(String.valueOf(entry.get("name")));
                }
                line(indent, with(tokens, comment));
            }
            case "EXIT" ->
                expressionLine(indent, List.of("EXIT"), text(entry.get("expression"), "expression"), comment);
            case "PRINT", "WRITE" ->
                expressionLine(
                        indent,
                        List.of(String.valueOf(entry.get("keyword"))),
                        text(entry.get("expression"), "expression"),
                        comment);
            case "INPUT" -> line(indent, with(List.of("INPUT", name(entry.get("name"), "name")), comment));
            default -> throw new IllegalArgumentException("Unknown keyword " + keyword);
        }
    }

    private void declaration(Map<?, ?> entry, String keyword, int indent, List<String> comment) {
        TypeSpec type = type(entry);
        List<String> tokens = new ArrayList<>(List.of(keyword, name(entry.get("name"), "name")));
        tokens.addAll(typeTokens(entry));
        if (entry.get("value") != null) {
            tokens.add(literal(entry.get("value"), type));
        }
        tokens.addAll(tagTokens(entry));
        line(indent, with(tokens, comment));
        if (entry.get("code") != null) {
            line(indent, List.of("BEGIN"));
            initializer(list(entry.get("code")), type, indent + 1);
            line(indent, List.of("END", keyword));
        }
    }

    /** The content lines of an initializer of an ARRAY, HASH or STRUCT. */
    private void initializer(List<?> entries, TypeSpec type, int indent) {
        for (Object item : entries) {
            Map<?, ?> entry = map(item);
            String keyword = name(entry.get("keyword"), "keyword");
            if (keyword.equals("LITERAL")) {
                TypeSpec element = type instanceof TypeSpec.Array array ? array.element() : null;
                if (entry.get("code") != null) {
                    line(indent, List.of("BEGIN"));
                    initializer(list(entry.get("code")), element, indent + 1);
                    line(indent, List.of("END"));
                } else {
                    List<String> values = new ArrayList<>();
                    for (Object value : list(entry.get("values"))) {
                        values.add(literal(value, element));
                    }
                    // Many values are spread over several lines, which the parser reads one after another
                    int width = WIDTH - indent;
                    for (int start = 0; start < values.size(); start += width) {
                        List<String> part = values.subList(start, Math.min(values.size(), start + width));
                        line(indent, part);
                    }
                }
            } else if (keyword.equals("ELEMENT")) {
                String key = name(entry.get("name"), "name");
                TypeSpec element = elementType(type, key);
                if (entry.get("code") != null) {
                    line(indent, List.of(key));
                    line(indent, List.of("BEGIN"));
                    initializer(list(entry.get("code")), element, indent + 1);
                    line(indent, List.of("END"));
                } else {
                    line(indent, with(List.of(key, literal(entry.get("value"), element)), comment(entry)));
                }
            } else {
                throw new IllegalArgumentException("Unknown keyword " + keyword + " in an initializer");
            }
        }
    }

    private TypeSpec elementType(TypeSpec type, String key) {
        return switch (type) {
            case TypeSpec.Hash hash -> hash.element();
            case TypeSpec.Struct struct ->
                structs.getOrDefault(struct.name(), Map.of()).get(key);
            case null, default -> null;
        };
    }

    // ------------------------------------------------------------------------------------------ types, values

    /** The type of an object from {@code type}, {@code length}, {@code subtype} and {@code struct}. */
    private static TypeSpec type(Map<?, ?> entry) {
        return TypeText.parse(String.join(" ", typeTokens(entry)));
    }

    private static List<String> typeTokens(Map<?, ?> entry) {
        List<String> tokens = new ArrayList<>();
        tokens.add(name(entry.get("type"), "type"));
        if (entry.get("length") != null) {
            tokens.add(String.valueOf(entry.get("length")));
        }
        if (entry.get("subtype") != null) {
            tokens.addAll(List.of(String.valueOf(entry.get("subtype")).split(" ")));
        }
        if (entry.get("struct") != null) {
            tokens.add(String.valueOf(entry.get("struct")));
        }
        return tokens;
    }

    private static List<String> tagTokens(Map<?, ?> entry) {
        List<String> tokens = new ArrayList<>();
        if (entry.get("tags") instanceof Map<?, ?> tags) {
            tags.forEach((language, name) -> tokens.add("TAG(" + language + ": " + quote(String.valueOf(name)) + ")"));
        }
        return tokens;
    }

    /**
     * A literal value as notation token. The type decides between CHAR, STRING and TIME for strings and between
     * INTEGER and FLOAT for numbers; without a type, strings are STRING values.
     */
    private static String literal(Object value, TypeSpec type) {
        Type.Simple simple = type instanceof TypeSpec.Simple s ? s.type() : null;
        return switch (value) {
            case Boolean b -> Notation.formatLiteral(BooleanValue.of(b));
            case Integer i when simple == Type.Simple.FLOAT -> Notation.formatLiteral(new FloatValue(i));
            case Long l when simple == Type.Simple.FLOAT -> Notation.formatLiteral(new FloatValue(l));
            case Integer i -> Notation.formatLiteral(new IntegerValue(i));
            case Long l -> Notation.formatLiteral(new IntegerValue(l));
            case Double d -> Notation.formatLiteral(new FloatValue(d));
            case String s when simple == Type.Simple.CHAR -> Notation.formatLiteral(new CharValue(s.codePointAt(0)));
            case String s
            when simple == Type.Simple.TIME ->
                Notation.formatLiteral(new TimeValue(Literals.parseTime(s, ZoneOffset.UTC)));
            case String s -> Notation.formatLiteral(new StringValue(s));
            default -> throw new IllegalArgumentException("Not a literal value: " + value);
        };
    }

    private static String quote(String text) {
        return Notation.formatLiteral(new StringValue(text));
    }

    // ------------------------------------------------------------------------------------------ YAML helpers

    private static Map<?, ?> map(Object value) {
        if (value instanceof Map<?, ?> map) {
            return map;
        }
        throw new IllegalArgumentException("A statement must be an object: " + value);
    }

    private static List<?> list(Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof List<?> list) {
            return list;
        }
        throw new IllegalArgumentException("Not a list: " + value);
    }

    private static String name(Object value, String key) {
        if (value == null || String.valueOf(value).isBlank()) {
            throw new IllegalArgumentException("The property " + key + " is missing");
        }
        return String.valueOf(value);
    }

    private static String text(Object value, String key) {
        return name(value, key);
    }
}
