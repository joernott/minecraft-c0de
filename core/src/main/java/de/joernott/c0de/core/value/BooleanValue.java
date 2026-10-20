package de.joernott.c0de.core.value;

/** A BOOLEAN value. */
public record BooleanValue(boolean value) implements Value {
    public static final BooleanValue TRUE = new BooleanValue(true);
    public static final BooleanValue FALSE = new BooleanValue(false);

    public static BooleanValue of(boolean value) {
        return value ? TRUE : FALSE;
    }

    @Override
    public Type type() {
        return Type.Simple.BOOLEAN;
    }

    @Override
    public String text() {
        return value ? "true" : "false";
    }
}
