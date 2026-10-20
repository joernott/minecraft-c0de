package de.joernott.c0de.core.value;

/** An INTEGER value (signed 64 bit). */
public record IntegerValue(long value) implements Value {
    @Override
    public Type type() {
        return Type.Simple.INTEGER;
    }

    @Override
    public String text() {
        return Long.toString(value);
    }
}
