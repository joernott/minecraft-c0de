package de.joernott.c0de.core.library.standard;

import static de.joernott.c0de.core.library.standard.Functions.ANY;
import static de.joernott.c0de.core.library.standard.Functions.BOOLEAN;
import static de.joernott.c0de.core.library.standard.Functions.CHAR;
import static de.joernott.c0de.core.library.standard.Functions.INTEGER;
import static de.joernott.c0de.core.library.standard.Functions.MAX_TEXT_LENGTH;
import static de.joernott.c0de.core.library.standard.Functions.NUMBER;
import static de.joernott.c0de.core.library.standard.Functions.TEXT;
import static de.joernott.c0de.core.library.standard.Functions.TIME;
import static de.joernott.c0de.core.library.standard.Functions.codePoints;
import static de.joernott.c0de.core.library.standard.Functions.fromCodePoints;
import static de.joernott.c0de.core.library.standard.Functions.integer;
import static de.joernott.c0de.core.library.standard.Functions.invalid;
import static de.joernott.c0de.core.library.standard.Functions.nonNegative;
import static de.joernott.c0de.core.library.standard.Functions.optional;
import static de.joernott.c0de.core.library.standard.Functions.required;
import static de.joernott.c0de.core.library.standard.Functions.text;

import de.joernott.c0de.core.eval.Variable;
import de.joernott.c0de.core.library.Library;
import de.joernott.c0de.core.library.NativeCall;
import de.joernott.c0de.core.library.NativeFunction;
import de.joernott.c0de.core.library.NativeResult;
import de.joernott.c0de.core.value.BooleanValue;
import de.joernott.c0de.core.value.CharValue;
import de.joernott.c0de.core.value.FloatValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.Value;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * The library {@code c0de/strings} (docs/concept/10-lib-strings.md). Positions in texts start at 0 and count
 * Unicode characters (code points).
 */
public final class StringsLibrary implements Library {
    public static final String NAME = "c0de/strings";

    private static final Pattern INTEGER_TEXT = Pattern.compile("[-+]?\\d+");
    private static final Pattern FLOAT_TEXT = Pattern.compile("[-+]?(\\d+\\.?\\d*|\\.\\d+)([eE][-+]?\\d+)?");

    private static final Map<String, NativeFunction> FUNCTIONS = new Functions.Builder()
            .add("LEFT", List.of(required("text", "STRING", TEXT), required("length", "INTEGER", INTEGER)), call -> {
                int[] text = codePoints(text(call, "text"));
                int length = nonNegative(integer(call, "length"), "The length");
                return new StringValue(fromCodePoints(text, 0, Math.min(length, text.length)));
            })
            .add("RIGHT", List.of(required("text", "STRING", TEXT), required("length", "INTEGER", INTEGER)), call -> {
                int[] text = codePoints(text(call, "text"));
                int length = Math.min(nonNegative(integer(call, "length"), "The length"), text.length);
                return new StringValue(fromCodePoints(text, text.length - length, text.length));
            })
            .add(
                    "MID",
                    List.of(
                            required("text", "STRING", TEXT),
                            required("start", "INTEGER", INTEGER),
                            optional("length", "INTEGER", INTEGER)),
                    StringsLibrary::mid)
            .add(
                    "REPLACE",
                    List.of(
                            required("text", "STRING", TEXT),
                            required("start", "INTEGER", INTEGER),
                            required("insert", "STRING", TEXT)),
                    StringsLibrary::replace)
            .add(
                    "INSTR",
                    List.of(
                            required("text", "STRING", TEXT),
                            required("search", "STRING", TEXT),
                            optional("start", "INTEGER", INTEGER, new IntegerValue(0))),
                    StringsLibrary::instr)
            .add(
                    "UCASE",
                    List.of(required("text", "STRING", TEXT)),
                    call -> new StringValue(text(call, "text").toUpperCase(Locale.ROOT)))
            .add(
                    "LCASE",
                    List.of(required("text", "STRING", TEXT)),
                    call -> new StringValue(text(call, "text").toLowerCase(Locale.ROOT)))
            .add(
                    "SPACE",
                    List.of(required("count", "INTEGER", INTEGER)),
                    call -> new StringValue(" ".repeat(count(call))))
            .add(
                    "FILL",
                    List.of(required("count", "INTEGER", INTEGER), required("char", "CHAR", CHAR)),
                    call -> new StringValue(text(call, "char").repeat(count(call))))
            .add(
                    "ASC",
                    List.of(required("char", "CHAR", CHAR)),
                    call -> new IntegerValue(((CharValue) call.argument("char")).codePoint()))
            .add("CHR", List.of(required("code", "INTEGER", INTEGER)), call -> {
                long code = integer(call, "code");
                if (code < 0 || code > Character.MAX_CODE_POINT) {
                    throw invalid("Not a Unicode code point: " + code);
                }
                return new CharValue((int) code);
            })
            .add(
                    "STR",
                    List.of(required(
                            "value",
                            "BOOLEAN|INTEGER|FLOAT|CHAR|TIME",
                            BOOLEAN.or(NUMBER).or(CHAR).or(TIME))),
                    call -> new StringValue(call.argument("value").text()))
            .add("VAL", List.of(required("text", "STRING", TEXT)), List.of("value", "isInteger"), StringsLibrary::val)
            .add(
                    "HEX",
                    List.of(required("value", "INTEGER", INTEGER)),
                    call -> new StringValue(
                            Long.toHexString(integer(call, "value")).toUpperCase(Locale.ROOT)))
            .add(
                    "OCT",
                    List.of(required("value", "INTEGER", INTEGER)),
                    call -> new StringValue(Long.toOctalString(integer(call, "value"))))
            .add(
                    "FORMAT",
                    List.of(
                            required("value", "BOOLEAN|INTEGER|FLOAT|CHAR|STRING|TIME", ANY),
                            required("format", "STRING", TEXT)),
                    call -> new StringValue(Format.format(
                            call.argument("value"),
                            text(call, "format"),
                            call.process().zone())))
            .add(
                    "TO_JSON",
                    List.of(required("value", "any", ANY), optional("indent", "INTEGER", INTEGER, new IntegerValue(0))),
                    call -> {
                        long indent = integer(call, "indent");
                        if (indent < 0 || indent > 16) {
                            throw invalid("The indentation must be between 0 and 16: " + indent);
                        }
                        return new StringValue(DataFormats.writeJson(tree(call, DataFormats.JSON), (int) indent));
                    })
            .add(
                    "TO_YAML",
                    List.of(required("value", "any", ANY)),
                    call -> new StringValue(DataFormats.writeYaml(tree(call, DataFormats.YAML))))
            .add("FROM_JSON", readParameters(), List.of(), call -> read(call, DataFormats.JSON))
            .add("FROM_YAML", readParameters(), List.of(), call -> read(call, DataFormats.YAML))
            .build();

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Map<String, NativeFunction> functions() {
        return FUNCTIONS;
    }

    private static int count(NativeCall call) {
        long count = integer(call, "count");
        if (count < 0 || count > MAX_TEXT_LENGTH) {
            throw invalid("The count must be between 0 and " + MAX_TEXT_LENGTH + ": " + count);
        }
        return (int) count;
    }

    private static Value mid(NativeCall call) {
        int[] text = codePoints(text(call, "text"));
        int start = nonNegative(integer(call, "start"), "The start");
        if (start >= text.length) {
            return new StringValue("");
        }
        int length = call.provided("length") ? nonNegative(integer(call, "length"), "The length") : text.length;
        return new StringValue(fromCodePoints(text, start, (int) Math.min((long) start + length, text.length)));
    }

    private static Value replace(NativeCall call) {
        int[] text = codePoints(text(call, "text"));
        int start = nonNegative(integer(call, "start"), "The start");
        if (start > text.length) {
            throw invalid("The start " + start + " is behind the end of the text");
        }
        int[] insert = codePoints(text(call, "insert"));
        int[] result = text.clone();
        for (int i = 0; i < insert.length && start + i < result.length; i++) {
            result[start + i] = insert[i];
        }
        return new StringValue(new String(result, 0, result.length));
    }

    private static Value instr(NativeCall call) {
        int[] text = codePoints(text(call, "text"));
        int[] search = codePoints(text(call, "search"));
        int start = nonNegative(integer(call, "start"), "The start");
        for (int i = start; i + search.length <= text.length; i++) {
            boolean match = true;
            for (int j = 0; j < search.length && match; j++) {
                match = text[i + j] == search[j];
            }
            if (match) {
                return new IntegerValue(i);
            }
        }
        return new IntegerValue(-1);
    }

    private static NativeResult val(NativeCall call) {
        String text = text(call, "text").trim();
        if (INTEGER_TEXT.matcher(text).matches()) {
            try {
                return NativeResult.of(
                        Map.of("value", new FloatValue(Long.parseLong(text)), "isInteger", BooleanValue.TRUE));
            } catch (NumberFormatException tooLarge) {
                // Continues as FLOAT
            }
        }
        if (FLOAT_TEXT.matcher(text).matches()) {
            return NativeResult.of(
                    Map.of("value", new FloatValue(Double.parseDouble(text)), "isInteger", BooleanValue.FALSE));
        }
        throw invalid("Not a number: " + text);
    }

    private static Object tree(NativeCall call, String language) {
        Object tree = DataFormats.toTree(call.argument("value"), language);
        String path = call.tags("value").get(language);
        return path == null ? tree : DataFormats.wrap(tree, path);
    }

    private static List<NativeFunction.Parameter> readParameters() {
        return List.of(
                required("source", "STRING", TEXT), NativeFunction.Parameter.reference("destination", "a variable"));
    }

    private static NativeResult read(NativeCall call, String language) {
        Variable destination = call.reference("destination").orElseThrow();
        Object document = DataFormats.parse(text(call, "source"), language);
        String path = destination.tags().get(language);
        Object node = path == null ? document : DataFormats.select(document, path);
        Value value = DataFormats.read(node, destination.value(), destination.type(), language);
        // Only set when the whole document has been read, so that errors leave the variable unchanged
        destination.set(value);
        return NativeResult.none();
    }
}
