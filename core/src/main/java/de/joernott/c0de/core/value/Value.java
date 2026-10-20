package de.joernott.c0de.core.value;

/**
 * A value of the language. Values of simple types and strings are immutable; arrays, hashes and structs
 * are mutable and copied when they are assigned ({@link #copy()}).
 */
public sealed interface Value
        permits BooleanValue,
                IntegerValue,
                FloatValue,
                CharValue,
                TimeValue,
                StringValue,
                ArrayValue,
                HashValue,
                StructValue {
    /** The type of the value. Strings have the type STRING without length limit. */
    Type type();

    /** A deep copy of the value; immutable values return themselves. */
    default Value copy() {
        return this;
    }

    /** The text representation, used by the ⊕ operator and STR. */
    String text();
}
