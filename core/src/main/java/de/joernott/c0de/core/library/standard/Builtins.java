package de.joernott.c0de.core.library.standard;

import static de.joernott.c0de.core.library.standard.Functions.ANY;
import static de.joernott.c0de.core.library.standard.Functions.ARRAY;
import static de.joernott.c0de.core.library.standard.Functions.HASH;
import static de.joernott.c0de.core.library.standard.Functions.INTEGER;
import static de.joernott.c0de.core.library.standard.Functions.TEXT;
import static de.joernott.c0de.core.library.standard.Functions.integer;
import static de.joernott.c0de.core.library.standard.Functions.invalid;
import static de.joernott.c0de.core.library.standard.Functions.optional;
import static de.joernott.c0de.core.library.standard.Functions.required;
import static de.joernott.c0de.core.library.standard.Functions.text;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.library.Library;
import de.joernott.c0de.core.library.NativeCall;
import de.joernott.c0de.core.library.NativeFunction;
import de.joernott.c0de.core.library.NativeResult;
import de.joernott.c0de.core.value.ArrayValue;
import de.joernott.c0de.core.value.BooleanValue;
import de.joernott.c0de.core.value.HashValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0de.core.value.Values;
import java.util.List;
import java.util.Map;

/** The built-in functions, which are available without INCLUDE (docs/concept/08-builtin-functions.md). */
public final class Builtins implements Library {
    private static final Map<String, NativeFunction> FUNCTIONS = new Functions.Builder()
            .add(
                    "LEN",
                    List.of(required(
                            "value", "STRING|ARRAY|HASH", TEXT.or(ARRAY).or(HASH))),
                    Builtins::len)
            .add(
                    "HAS_KEY",
                    List.of(required("hash", "HASH", HASH), required("key", "STRING", TEXT)),
                    call -> BooleanValue.of(
                            ((HashValue) call.argument("hash")).elements().containsKey(text(call, "key"))))
            .add(
                    "REMOVE",
                    List.of(
                            required("container", "ARRAY|HASH", ARRAY.or(HASH)),
                            optional("index", "INTEGER", INTEGER),
                            optional("key", "STRING", TEXT)),
                    Builtins::remove)
            .add(
                    "INSERT",
                    List.of(
                            required("array", "ARRAY", ARRAY),
                            required("index", "INTEGER", INTEGER),
                            required("value", "the type of the elements", ANY)),
                    Builtins::insert)
            .add(
                    "CLEAR",
                    List.of(required("container", "ARRAY|HASH", ARRAY.or(HASH))),
                    call -> call.argument("container").type().defaultValue())
            .add(
                    "SWAP",
                    List.of(required("a", "any", ANY), required("b", "the type of a", ANY)),
                    List.of("a", "b"),
                    Builtins::swap)
            .add(
                    "RAISE",
                    List.of(
                            required("code", "INTEGER", INTEGER),
                            optional("message", "STRING", TEXT, new StringValue(""))),
                    List.of(),
                    Builtins::raise)
            .build();

    @Override
    public String name() {
        return "";
    }

    @Override
    public Map<String, NativeFunction> functions() {
        return FUNCTIONS;
    }

    private static Value len(NativeCall call) {
        return new IntegerValue(
                switch (call.argument("value")) {
                    case ArrayValue array -> array.elements().size();
                    case HashValue hash -> hash.elements().size();
                    case Value text -> text.text().codePointCount(0, text.text().length());
                });
    }

    private static Value remove(NativeCall call) {
        return switch (call.argument("container")) {
            case ArrayValue array -> {
                if (!call.provided("index")) {
                    throw invalid("REMOVE needs an index for an array");
                }
                long index = integer(call, "index");
                if (index < 0 || index >= array.elements().size()) {
                    throw C0deException.of(
                            ErrorCode.INDEX_OUT_OF_BOUNDS,
                            "The index " + index + " is outside of 0 to "
                                    + (array.elements().size() - 1));
                }
                ArrayValue result = array.copy();
                result.elements().remove((int) index);
                yield result;
            }
            case HashValue hash -> {
                if (!call.provided("key")) {
                    throw invalid("REMOVE needs a key for a hash");
                }
                String key = text(call, "key");
                if (!hash.elements().containsKey(key)) {
                    throw C0deException.of(ErrorCode.UNKNOWN_KEY, "The hash has no key \"" + key + "\"");
                }
                HashValue result = hash.copy();
                result.elements().remove(key);
                yield result;
            }
            default -> throw new IllegalStateException();
        };
    }

    private static Value insert(NativeCall call) {
        ArrayValue array = (ArrayValue) call.argument("array");
        long index = integer(call, "index");
        if (index < 0 || index > array.elements().size()) {
            throw C0deException.of(
                    ErrorCode.INDEX_OUT_OF_BOUNDS,
                    "The index " + index + " is outside of 0 to "
                            + array.elements().size());
        }
        Value value = Values.convert(call.argument("value"), array.elementType());
        ArrayValue result = array.copy();
        result.elements().add((int) index, value);
        return result;
    }

    private static NativeResult swap(NativeCall call) {
        Value a = call.argument("a");
        Value b = call.argument("b");
        if (!Values.sameKind(a.type(), b.type())) {
            throw C0deException.of(ErrorCode.TYPE_MISMATCH, "A " + a.type() + " can't be swapped with a " + b.type());
        }
        return NativeResult.of(Map.of("a", b, "b", a));
    }

    private static NativeResult raise(NativeCall call) {
        long code = integer(call, "code");
        if (code < ErrorCode.FIRST_USER_CODE) {
            throw invalid(
                    "The code of a user-defined error must be " + ErrorCode.FIRST_USER_CODE + " or higher: " + code);
        }
        return NativeResult.raise(code, text(call, "message"));
    }
}
