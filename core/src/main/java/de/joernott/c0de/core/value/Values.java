package de.joernott.c0de.core.value;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import java.util.List;
import java.util.Map;

/** Type checks, conversions and comparisons of values. */
public final class Values {
    private Values() {}

    /**
     * Converts a value for storing it in a variable, element or parameter of the target type, following the
     * rules of the ASSIGN statement: the types must match, except that an INTEGER may be stored in a FLOAT
     * and a CHAR in a STRING. Arrays and hashes are converted element by element. The result is always a
     * copy, so the original value is not shared.
     *
     * @throws C0deException TYPE_MISMATCH if the value doesn't fit the type, STRING_TOO_LONG if a string
     *     exceeds the length limit of the type
     */
    public static Value convert(Value value, Type target) {
        return switch (target) {
            case Type.Simple simple -> convertSimple(value, simple);
            case Type.Str str -> convertString(value, str);
            case Type.Array array -> {
                if (!(value instanceof ArrayValue source)) {
                    throw mismatch(value, target);
                }
                List<Value> elements = source.elements().stream()
                        .map(e -> convert(e, array.element()))
                        .toList();
                yield new ArrayValue(array.element(), elements);
            }
            case Type.Hash hash -> {
                if (!(value instanceof HashValue source)) {
                    throw mismatch(value, target);
                }
                HashValue result = new HashValue(hash.element());
                for (Map.Entry<String, Value> entry : source.elements().entrySet()) {
                    result.elements().put(entry.getKey(), convert(entry.getValue(), hash.element()));
                }
                yield result;
            }
            case Type.Struct struct -> {
                if (!(value instanceof StructValue source)
                        || !source.definition().equals(struct.definition())) {
                    throw mismatch(value, target);
                }
                yield source.copy();
            }
        };
    }

    /** True if the value can be stored in the type (see {@link #convert}), ignoring string length limits. */
    public static boolean fits(Value value, Type target) {
        try {
            convert(value, withoutLengthLimits(target));
            return true;
        } catch (C0deException e) {
            return false;
        }
    }

    private static Type withoutLengthLimits(Type type) {
        return switch (type) {
            case Type.Str str -> Type.Str.UNLIMITED;
            case Type.Array array -> new Type.Array(withoutLengthLimits(array.element()));
            case Type.Hash hash -> new Type.Hash(withoutLengthLimits(hash.element()));
            default -> type;
        };
    }

    private static Value convertSimple(Value value, Type.Simple target) {
        if (value.type() == target) {
            return value;
        }
        if (target == Type.Simple.FLOAT && value instanceof IntegerValue integer) {
            return new FloatValue(integer.value());
        }
        throw mismatch(value, target);
    }

    private static Value convertString(Value value, Type.Str target) {
        StringValue string = switch (value) {
            case StringValue s -> s;
            case CharValue c -> new StringValue(c.text());
            default -> throw mismatch(value, target);
        };
        if (target.maxLength() != null && string.length() > target.maxLength()) {
            throw C0deException.of(
                    ErrorCode.STRING_TOO_LONG,
                    "The text has " + string.length() + " characters, but at most " + target.maxLength()
                            + " are allowed");
        }
        return string;
    }

    /**
     * Compares two values with = (equal) and ≠. INTEGER and FLOAT are compared numerically, a CHAR is
     * compared with a STRING as text. All other values must have the same type.
     */
    public static boolean equal(Value left, Value right) {
        if (isNumber(left) && isNumber(right)) {
            if (left instanceof IntegerValue a && right instanceof IntegerValue b) {
                return a.value() == b.value();
            }
            return toDouble(left) == toDouble(right);
        }
        if (isText(left) && isText(right)) {
            return left.text().equals(right.text());
        }
        if (!sameKind(left.type(), right.type())) {
            throw C0deException.of(
                    ErrorCode.TYPE_MISMATCH, "A " + left.type() + " can't be compared with a " + right.type());
        }
        return left.equals(right);
    }

    /**
     * Compares two values with &lt;, &gt;, ≤ and ≥: numbers numerically, CHAR and STRING lexicographically (by
     * code points), TIME chronologically.
     *
     * @return a negative number, zero or a positive number if left is less than, equal to or greater than right
     */
    public static int compare(Value left, Value right) {
        if (isNumber(left) && isNumber(right)) {
            if (left instanceof IntegerValue a && right instanceof IntegerValue b) {
                return Long.compare(a.value(), b.value());
            }
            return Double.compare(toDouble(left), toDouble(right));
        }
        if (isText(left) && isText(right)) {
            return compareText(left.text(), right.text());
        }
        if (left instanceof TimeValue a && right instanceof TimeValue b) {
            return Long.compare(a.millis(), b.millis());
        }
        throw C0deException.of(
                ErrorCode.TYPE_MISMATCH, "A " + left.type() + " can't be ordered against a " + right.type());
    }

    private static int compareText(String a, String b) {
        int[] x = a.codePoints().toArray();
        int[] y = b.codePoints().toArray();
        for (int i = 0; i < Math.min(x.length, y.length); i++) {
            if (x[i] != y[i]) {
                return Integer.compare(x[i], y[i]);
            }
        }
        return Integer.compare(x.length, y.length);
    }

    /** True if both types are the same apart from string length limits. */
    public static boolean sameKind(Type a, Type b) {
        return withoutLengthLimits(a).equals(withoutLengthLimits(b));
    }

    public static boolean isNumber(Value value) {
        return value instanceof IntegerValue || value instanceof FloatValue;
    }

    private static boolean isText(Value value) {
        return value instanceof CharValue || value instanceof StringValue;
    }

    /** The numeric value of an INTEGER or FLOAT. */
    public static double toDouble(Value value) {
        return switch (value) {
            case IntegerValue integer -> integer.value();
            case FloatValue f -> f.value();
            default -> throw new IllegalArgumentException("Not a number: " + value);
        };
    }

    private static C0deException mismatch(Value value, Type target) {
        return C0deException.of(ErrorCode.TYPE_MISMATCH, "A " + value.type() + " doesn't fit into a " + target);
    }
}
