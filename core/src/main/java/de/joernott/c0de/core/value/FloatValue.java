package de.joernott.c0de.core.value;

/**
 * A FLOAT value (64 bit floating point). Records compare doubles with {@link Double#compare}; the =
 * operator compares numerically instead (see {@code Values.equal}).
 */
public record FloatValue(double value) implements Value {
    @Override
    public Type type() {
        return Type.Simple.FLOAT;
    }

    @Override
    public String text() {
        return Double.toString(value);
    }
}
