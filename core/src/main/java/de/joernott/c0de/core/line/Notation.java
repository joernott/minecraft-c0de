package de.joernott.c0de.core.line;

import de.joernott.c0de.core.line.Block.CommentBlock;
import de.joernott.c0de.core.line.Block.KeywordBlock;
import de.joernott.c0de.core.line.Block.LabelBlock;
import de.joernott.c0de.core.line.Block.LiteralBlock;
import de.joernott.c0de.core.line.Block.OperatorBlock;
import de.joernott.c0de.core.line.Block.TagBlock;
import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.core.value.BooleanValue;
import de.joernott.c0de.core.value.CharValue;
import de.joernott.c0de.core.value.FloatValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.Literals;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.TimeValue;
import de.joernott.c0de.core.value.Value;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A text notation of sheets, used in tests and log output. It follows the examples of the concept:
 *
 * <pre>
 * PROGRAM demo
 * NOOP
 * FUNCTION main
 *     PARAMETER count INTEGER 3
 * BEGIN
 *     ASSIGN total total + values[i] ;   // comment
 * END FUNCTION
 * </pre>
 *
 * <ul>
 *   <li>Every text line is a line of the sheet, starting with z=0. A blank text line is an empty line.
 *   <li>Every four spaces of indentation move the first block one block to the right of x=5.
 *   <li>Every token is one block: keywords ({@code PROGRAM}, {@code INTEGER}, ...), operators ({@code +},
 *       {@code ≠} or {@code <>}, {@code NOT}, {@code (}, {@code ;}, ...), literals ({@code 42}, {@code -1},
 *       {@code 3.14}, {@code TRUE}, {@code 'a'}, {@code "text"}, {@code @2026-10-19T12:05:09Z}) and labels
 *       ({@code name}, {@code c0de/strings/MID}, {@code matrix[i][2]}, {@code inventory["diamond"]},
 *       {@code error.code}).
 *   <li>{@code // text} is a COMMENT block with the rest of the line as text, {@code COMMENT("text")} a COMMENT
 *       block followed by more blocks, {@code TAG(json: "name")} a TAG block.
 *   <li>{@code LABEL}, {@code LITERAL} and {@code OPERATOR} are blocks without name, value or symbol.
 *   <li>{@code _} is a gap: no block at this position.
 * </ul>
 */
public final class Notation {
    private static final Pattern INTEGER = Pattern.compile("-?\\d+");
    private static final Pattern FLOAT = Pattern.compile("-?\\d+\\.\\d+([eE][-+]?\\d+)?");
    private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_/]+");
    private static final Pattern TAG = Pattern.compile("TAG\\(\\s*(\\w*)\\s*:\\s*\"(.*)\"\\s*\\)");
    private static final Pattern COMMENT = Pattern.compile("COMMENT\\(\\s*\"(.*)\"\\s*\\)");

    private Notation() {}

    /** Parses a sheet. TIME literals without an offset are in UTC. */
    public static Sheet parse(String text) {
        return parse(text, ZoneOffset.UTC);
    }

    /** Parses a sheet. TIME literals without an offset are in the given time zone. */
    public static Sheet parse(String text, ZoneId zone) {
        String[] rows = text.split("\\R", -1);
        // A trailing line break doesn't add an empty line
        int count = rows.length > 0 && rows[rows.length - 1].isBlank() ? rows.length - 1 : rows.length;
        List<Line> lines = new ArrayList<>();
        for (int z = 0; z < count; z++) {
            lines.add(parseLine(z, rows[z], zone));
        }
        return new Sheet(lines);
    }

    /** Parses one line of the notation. */
    public static Line parseLine(int z, String row, ZoneId zone) {
        List<Line.Cell> cells = new ArrayList<>();
        int i = 0;
        int spaces = 0;
        while (i < row.length() && row.charAt(i) == ' ') {
            i++;
            spaces++;
        }
        int x = SheetGeometry.CODE_MIN_X + spaces / 4;
        while (i < row.length()) {
            char c = row.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            if (row.startsWith("//", i)) {
                cells.add(
                        new Line.Cell(x++, new CommentBlock(row.substring(i + 2).trim())));
                break;
            }
            int end = tokenEnd(row, i);
            String token = row.substring(i, end);
            i = end;
            if (!token.equals("_")) {
                cells.add(new Line.Cell(x, block(token, zone)));
            }
            x++;
        }
        return new Line(z, cells);
    }

    private static int tokenEnd(String row, int start) {
        char first = row.charAt(start);
        if (first == '"' || first == '\'') {
            return closingQuote(row, start) + 1;
        }
        if (first == '(' || first == ')' || first == ';') {
            return start + 1;
        }
        if (row.startsWith("TAG(", start) || row.startsWith("COMMENT(", start)) {
            int i = row.indexOf('(', start) + 1;
            while (i < row.length() && row.charAt(i) != ')') {
                i = row.charAt(i) == '"' ? closingQuote(row, i) + 1 : i + 1;
            }
            return Math.min(i + 1, row.length());
        }
        int i = start;
        while (i < row.length()) {
            char c = row.charAt(i);
            if (Character.isWhitespace(c) || c == '(' || c == ')' || c == ';') {
                break;
            }
            if (c == '[') {
                // Index of a label, may contain quoted keys
                while (i < row.length() && row.charAt(i) != ']') {
                    i = row.charAt(i) == '"' ? closingQuote(row, i) + 1 : i + 1;
                }
            }
            i++;
        }
        return i;
    }

    private static int closingQuote(String row, int start) {
        char quote = row.charAt(start);
        int i = start + 1;
        while (i < row.length() && row.charAt(i) != quote) {
            i += row.charAt(i) == '\\' ? 2 : 1;
        }
        if (i >= row.length()) {
            throw new IllegalArgumentException("Missing closing quote: " + row);
        }
        return i;
    }

    private static Block block(String token, ZoneId zone) {
        Matcher tag = TAG.matcher(token);
        if (tag.matches()) {
            return new TagBlock(tag.group(1).isEmpty() ? null : tag.group(1), unescape(tag.group(2)));
        }
        Matcher comment = COMMENT.matcher(token);
        if (comment.matches()) {
            return new CommentBlock(unescape(comment.group(1)));
        }
        Optional<Value> literal = literal(token, zone);
        if (literal.isPresent()) {
            return new LiteralBlock(literal.get());
        }
        switch (token) {
            case "LITERAL":
                return new LiteralBlock(null);
            case "LABEL":
                return new LabelBlock(null);
            case "OPERATOR":
                return new OperatorBlock(null);
            default:
                break;
        }
        for (Keyword keyword : Keyword.values()) {
            if (keyword.name().equals(token)) {
                return new KeywordBlock(keyword);
            }
        }
        Optional<Operator> operator = Operator.ofSymbol(token);
        if (operator.isPresent()) {
            return new OperatorBlock(operator.get());
        }
        return label(token);
    }

    private static Optional<Value> literal(String token, ZoneId zone) {
        if (token.startsWith("\"")) {
            return Optional.of(new StringValue(unescape(token.substring(1, token.length() - 1))));
        }
        if (token.startsWith("'")) {
            String text = unescape(token.substring(1, token.length() - 1));
            if (text.codePointCount(0, text.length()) != 1) {
                throw new IllegalArgumentException("A CHAR literal must contain exactly one character: " + token);
            }
            return Optional.of(new CharValue(text.codePointAt(0)));
        }
        if (token.startsWith("@")) {
            return Optional.of(new TimeValue(Literals.parseTime(token.substring(1), zone)));
        }
        if (token.equals("TRUE") || token.equals("FALSE")) {
            return Optional.of(BooleanValue.of(token.equals("TRUE")));
        }
        if (INTEGER.matcher(token).matches()) {
            return Optional.of(new IntegerValue(Long.parseLong(token)));
        }
        if (FLOAT.matcher(token).matches()) {
            return Optional.of(new FloatValue(Double.parseDouble(token)));
        }
        return Optional.empty();
    }

    private static LabelBlock label(String token) {
        Matcher name = NAME.matcher(token);
        if (!name.lookingAt()) {
            throw new IllegalArgumentException("Not a block: " + token);
        }
        return new LabelBlock(name.group(), IndexText.parse(token.substring(name.end())));
    }

    private static String unescape(String text) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < text.length()) {
                char next = text.charAt(++i);
                result.append(next == 'n' ? '\n' : next);
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /** The notation of a sheet, e.g. for log output and error messages. */
    public static String format(Sheet sheet) {
        StringBuilder text = new StringBuilder();
        for (Line line : sheet.lines()) {
            text.append(format(line)).append('\n');
        }
        return text.toString();
    }

    /** The notation of one line. Gaps between blocks are written as {@code _}. */
    public static String format(Line line) {
        if (line.isEmpty()) {
            return "";
        }
        StringBuilder text = new StringBuilder();
        int x = line.cells().get(0).x();
        text.append("    ".repeat(Math.max(0, x - SheetGeometry.CODE_MIN_X)));
        for (Line.Cell cell : line.cells()) {
            while (x < cell.x()) {
                text.append("_ ");
                x++;
            }
            text.append(format(cell.block())).append(' ');
            x++;
        }
        return text.toString().stripTrailing();
    }

    private static String format(Block block) {
        return switch (block) {
            case KeywordBlock keyword -> keyword.keyword().name();
            case LabelBlock label -> label.name() == null ? "LABEL" : label.name() + IndexText.format(label.index());
            case LiteralBlock literal -> literal.value() == null ? "LITERAL" : formatLiteral(literal.value());
            case OperatorBlock operator ->
                operator.operator() == null ? "OPERATOR" : operator.operator().symbol();
            case CommentBlock comment -> "COMMENT(\"" + escape(comment.text()) + "\")";
            case TagBlock tag ->
                "TAG(" + (tag.language() == null ? "" : tag.language()) + ": \""
                        + escape(tag.name() == null ? "" : tag.name()) + "\")";
        };
    }

    /** A literal value in the notation. */
    public static String formatLiteral(Value value) {
        return switch (value) {
            case StringValue string -> "\"" + escape(string.value()) + "\"";
            case CharValue c -> "'" + escape(c.text()) + "'";
            case BooleanValue b -> b.value() ? "TRUE" : "FALSE";
            case TimeValue time -> "@" + time.text();
            default -> value.text();
        };
    }

    private static String escape(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("'", "\\'")
                .replace("\n", "\\n");
    }
}
