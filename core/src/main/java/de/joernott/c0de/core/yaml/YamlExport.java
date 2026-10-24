package de.joernott.c0de.core.yaml;

import de.joernott.c0de.core.line.Block;
import de.joernott.c0de.core.line.Keyword;
import de.joernott.c0de.core.line.Line;
import de.joernott.c0de.core.line.Notation;
import de.joernott.c0de.core.line.Sheet;
import de.joernott.c0de.core.program.Initializer;
import de.joernott.c0de.core.program.Program;
import de.joernott.c0de.core.program.ProgramParser;
import de.joernott.c0de.core.program.Statement;
import de.joernott.c0de.core.program.Statements;
import de.joernott.c0de.core.program.TypeSpec;
import de.joernott.c0de.core.program.TypeText;
import de.joernott.c0de.core.value.BooleanValue;
import de.joernott.c0de.core.value.CharValue;
import de.joernott.c0de.core.value.FloatValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.TimeValue;
import de.joernott.c0de.core.value.Value;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Writes the program on a sheet as YAML (docs/concept/17-yaml-format.md). */
final class YamlExport {
    private final Sheet sheet;

    private YamlExport(Sheet sheet) {
        this.sheet = sheet;
    }

    /**
     * The program as a tree of maps and lists.
     *
     * @throws IllegalArgumentException if the program is not structurally valid; the message names the line
     */
    static Map<String, Object> export(Sheet sheet) {
        Program program = ProgramParser.parse(sheet);
        if (!program.errors().isEmpty()) {
            Statement.Invalid invalid = program.errors().get(0);
            int line = invalid.error().line() < 0
                    ? invalid.line()
                    : invalid.error().line();
            throw new IllegalArgumentException("The program can't be exported, line " + line + " is invalid: "
                    + invalid.error().getMessage());
        }
        return new YamlExport(sheet).program(program);
    }

    private Map<String, Object> program(Program program) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("PROGRAM", program.name());
        List<Object> includes = new ArrayList<>();
        List<Statement> code = new ArrayList<>();
        int from = 2;
        for (Statement statement : program.statements()) {
            if (statement instanceof Statement.Include include) {
                includes.add(include.library());
                from = include.line() + 1;
            } else {
                code.add(statement);
            }
        }
        if (!includes.isEmpty()) {
            root.put("INCLUDE", includes);
        }
        root.put("code", entries(code, from, program.endLine()));
        return root;
    }

    /** The statements of the lines from {@code from} to {@code to} (exclusive), including NOOP and comment lines. */
    private List<Object> entries(List<Statement> statements, int from, int to) {
        List<Object> entries = new ArrayList<>();
        int z = from;
        for (Statement statement : statements) {
            for (; z < statement.line(); z++) {
                plainLine(z, entries);
            }
            entries.add(statement(statement));
            z = Statements.endLine(statement) + 1;
        }
        for (; z < to; z++) {
            plainLine(z, entries);
        }
        return entries;
    }

    /** A NOOP or comment line, which the parser leaves out. */
    private void plainLine(int z, List<Object> entries) {
        List<Line.Cell> cells = sheet.line(z).cells();
        if (cells.isEmpty()) {
            return;
        }
        Map<String, Object> entry = new LinkedHashMap<>();
        if (cells.get(0).block() instanceof Block.CommentBlock comment) {
            entry.put("keyword", "COMMENT");
            entry.put("text", comment.text());
            String disabled = disabled(cells, 0);
            if (!disabled.isEmpty()) {
                entry.put("disabled", disabled);
            }
            entries.add(entry);
        } else if (cells.get(0).block() instanceof Block.KeywordBlock(Keyword keyword) && keyword == Keyword.NOOP) {
            entry.put("keyword", "NOOP");
            entries.add(entry);
        }
    }

    /** The blocks after the COMMENT block at an index, as notation. */
    private static String disabled(List<Line.Cell> cells, int comment) {
        List<Line.Cell> rest = cells.subList(comment + 1, cells.size());
        return rest.isEmpty() ? "" : Notation.format(new Line(0, rest)).strip();
    }

    /** Adds the COMMENT block at the end of a line, and the blocks after it, to the object of the line. */
    private Map<String, Object> commented(Map<String, Object> entry, int z) {
        List<Line.Cell> cells = sheet.line(z).cells();
        for (int i = 1; i < cells.size(); i++) {
            if (cells.get(i).block() instanceof Block.CommentBlock comment) {
                entry.put("comment", comment.text());
                String disabled = disabled(cells, i);
                if (!disabled.isEmpty()) {
                    entry.put("disabled", disabled);
                }
                break;
            }
        }
        return entry;
    }

    private static Map<String, Object> entry(String keyword) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("keyword", keyword);
        return entry;
    }

    private Map<String, Object> statement(Statement statement) {
        Map<String, Object> entry;
        switch (statement) {
            case Statement.StructDeclaration struct -> {
                entry = entry("STRUCT");
                entry.put("name", struct.name());
                List<Object> elements = new ArrayList<>();
                for (Statement.StructDeclaration.Element element : struct.elements()) {
                    Map<String, Object> item = entry("ELEMENT");
                    item.put("name", element.name());
                    type(item, element.type());
                    tags(item, element.tags());
                    elements.add(commented(item, element.line()));
                }
                entry.put("code", elements);
            }
            case Statement.Declaration declaration -> {
                entry = entry(declaration.constant() ? "CONSTANT" : "VARIABLE");
                entry.put("name", declaration.name());
                type(entry, declaration.type());
                if (declaration.literal() != null) {
                    entry.put("value", value(declaration.literal()));
                }
                if (declaration.initializer() != null) {
                    entry.put("code", initializer(declaration.initializer()));
                }
                tags(entry, declaration.tags());
            }
            case Statement.FunctionDeclaration function -> {
                entry = entry("FUNCTION");
                entry.put("name", function.name());
                List<Object> parameters = new ArrayList<>();
                for (Statement.FunctionDeclaration.Parameter parameter : function.parameters()) {
                    Map<String, Object> item = entry("PARAMETER");
                    item.put("name", parameter.name());
                    type(item, parameter.type());
                    if (parameter.defaultValue() != null) {
                        item.put("value", value(parameter.defaultValue()));
                    }
                    parameters.add(commented(item, parameter.line()));
                }
                List<Object> results = new ArrayList<>();
                for (Statement.FunctionDeclaration.Result result : function.results()) {
                    Map<String, Object> item = entry("RESULT");
                    item.put("name", result.name());
                    type(item, result.type());
                    results.add(commented(item, result.line()));
                }
                if (!parameters.isEmpty()) {
                    entry.put("parameters", parameters);
                }
                if (!results.isEmpty()) {
                    entry.put("results", results);
                }
                entry.put("code", block(function.body()));
            }
            case Statement.Assign assign -> {
                entry = entry("ASSIGN");
                entry.put("name", ExpressionText.label(assign.target()));
                entry.put("expression", ExpressionText.format(assign.value()));
            }
            case Statement.Call call -> {
                entry = entry("CALL");
                entry.put("name", call.function());
                if (call.errorLabel() != null) {
                    entry.put("error", call.errorLabel());
                }
                List<Object> parameters = new ArrayList<>();
                for (Statement.Call.CallParameter parameter : call.parameters()) {
                    Map<String, Object> item = entry("PARAMETER");
                    item.put("name", parameter.name());
                    item.put("expression", ExpressionText.format(parameter.value()));
                    parameters.add(commented(item, parameter.line()));
                }
                List<Object> results = new ArrayList<>();
                for (Statement.Call.CallResult result : call.results()) {
                    Map<String, Object> item = entry("RESULT");
                    item.put("name", result.name());
                    item.put("target", ExpressionText.label(result.target()));
                    results.add(commented(item, result.line()));
                }
                if (!parameters.isEmpty()) {
                    entry.put("parameters", parameters);
                }
                if (!results.isEmpty()) {
                    entry.put("results", results);
                }
            }
            case Statement.If condition -> {
                entry = entry("IF");
                entry.put("expression", ExpressionText.format(condition.condition()));
                entry.put("code", block(condition.then()));
                if (condition.otherwise() != null) {
                    entry.put("else", block(condition.otherwise()));
                }
            }
            case Statement.For loop -> {
                entry = entry("FOR");
                entry.put("name", loop.variable());
                entry.put("type", loop.type().name());
                entry.put("from", ExpressionText.format(loop.from()));
                entry.put("to", ExpressionText.format(loop.to()));
                if (loop.step() != null) {
                    entry.put("step", ExpressionText.format(loop.step()));
                }
                entry.put("code", block(loop.body()));
            }
            case Statement.Each loop -> {
                entry = entry("EACH");
                entry.put("name", loop.variable());
                entry.put("iterable", ExpressionText.label(loop.iterable()));
                entry.put("code", block(loop.body()));
            }
            case Statement.While loop -> {
                entry = entry("WHILE");
                entry.put("expression", ExpressionText.format(loop.condition()));
                entry.put("code", block(loop.body()));
            }
            case Statement.Repeat loop -> {
                entry = entry("REPEAT");
                entry.put("code", block(loop.body()));
                entry.put("expression", ExpressionText.format(loop.condition()));
            }
            case Statement.Break breakStatement -> {
                entry = entry("BREAK");
                if (breakStatement.condition() != null) {
                    entry.put("expression", ExpressionText.format(breakStatement.condition()));
                }
            }
            case Statement.Return returnStatement -> {
                entry = entry("RETURN");
                if (returnStatement.error() != null) {
                    entry.put("name", ExpressionText.label(returnStatement.error()));
                }
            }
            case Statement.Exit exit -> {
                entry = entry("EXIT");
                entry.put("expression", ExpressionText.format(exit.code()));
            }
            case Statement.Include include -> throw new IllegalStateException("INCLUDE in a code block");
            case Statement.Invalid invalid -> throw new IllegalStateException("Invalid statement");
        }
        return commented(entry, statement.line());
    }

    private List<Object> block(Statement.CodeBlock block) {
        return entries(block.statements(), block.beginLine() + 1, block.endLine());
    }

    private List<Object> initializer(Initializer initializer) {
        List<Object> entries = new ArrayList<>();
        for (Initializer.Entry item : initializer.entries()) {
            Map<String, Object> entry;
            switch (item) {
                case Initializer.Values values -> {
                    entry = entry("LITERAL");
                    entry.put(
                            "values",
                            values.values().stream().map(YamlExport::value).toList());
                }
                case Initializer.Nested nested -> {
                    entry = entry("LITERAL");
                    entry.put("code", initializer(nested.content()));
                }
                case Initializer.Keyed keyed -> {
                    entry = entry("ELEMENT");
                    entry.put("name", keyed.key());
                    entry.put("value", value(keyed.value()));
                }
                case Initializer.KeyedNested nested -> {
                    entry = entry("ELEMENT");
                    entry.put("name", nested.key());
                    entry.put("code", initializer(nested.content()));
                }
            }
            entries.add(commented(entry, item.line()));
        }
        return entries;
    }

    /**
     * The type of a declaration: {@code type} (the first type block), {@code length} (STRING length limit),
     * {@code subtype} (the type of the elements of an ARRAY or HASH, without a struct name) and {@code struct}
     * (the name of the struct the type ends with).
     */
    static void type(Map<String, Object> entry, TypeSpec type) {
        String text = TypeText.format(type);
        String struct = null;
        int position = text.indexOf("STRUCT ");
        if (position >= 0) {
            struct = text.substring(position + "STRUCT ".length());
            text = text.substring(0, position + "STRUCT".length());
        }
        String[] words = text.split(" ", 2);
        entry.put("type", words[0]);
        if (words.length > 1) {
            if (words[0].equals("STRING")) {
                entry.put("length", Long.parseLong(words[1]));
            } else {
                entry.put("subtype", words[1]);
            }
        }
        if (struct != null) {
            entry.put("struct", struct);
        }
    }

    private static void tags(Map<String, Object> entry, Map<String, String> tags) {
        if (!tags.isEmpty()) {
            entry.put("tags", new LinkedHashMap<>(new java.util.TreeMap<>(tags)));
        }
    }

    /** A literal value as a YAML scalar. */
    static Object value(Value value) {
        return switch (value) {
            case BooleanValue b -> b.value();
            case IntegerValue i -> i.value();
            case FloatValue f -> f.value();
            case CharValue c -> c.text();
            case StringValue s -> s.value();
            case TimeValue t -> t.text();
            default -> throw new IllegalArgumentException("A literal can't be " + value);
        };
    }
}
