package de.joernott.c0de.core.value;

/** A CHAR value: a single Unicode code point. */
public record CharValue(int codePoint) implements Value {
    public CharValue {
        if (!Character.isValidCodePoint(codePoint)) {
            throw new IllegalArgumentException("Not a Unicode code point: " + codePoint);
        }
    }

    @Override
    public Type type() {
        return Type.Simple.CHAR;
    }

    @Override
    public String text() {
        return Character.toString(codePoint);
    }
}
