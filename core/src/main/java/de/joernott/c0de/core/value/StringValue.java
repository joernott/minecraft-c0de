package de.joernott.c0de.core.value;

/** A STRING value. */
public record StringValue(String value) implements Value {
    public StringValue {
        if (value == null) {
            throw new IllegalArgumentException("A string must not be null");
        }
    }

    @Override
    public Type type() {
        return Type.Str.UNLIMITED;
    }

    @Override
    public String text() {
        return value;
    }

    /** The number of characters (Unicode code points). */
    public int length() {
        return value.codePointCount(0, value.length());
    }
}
