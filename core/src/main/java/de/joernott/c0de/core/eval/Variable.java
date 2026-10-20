package de.joernott.c0de.core.eval;

import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0de.core.value.Values;
import java.util.Map;

/**
 * A variable or constant: a name, a type and a value. The value of a variable can be replaced or changed
 * through an index; the value of a constant can't.
 */
public final class Variable {
    private final String name;
    private final Type type;
    private final boolean constant;
    private final Map<String, String> tags;
    private Value value;

    public Variable(String name, Type type, Value value, boolean constant, Map<String, String> tags) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.constant = constant;
        this.tags = Map.copyOf(tags);
    }

    /** A variable with the default value of its type and no tags. */
    public static Variable of(String name, Type type) {
        return new Variable(name, type, type.defaultValue(), false, Map.of());
    }

    public String name() {
        return name;
    }

    public Type type() {
        return type;
    }

    public boolean constant() {
        return constant;
    }

    /** The tags of the variable: language to path. */
    public Map<String, String> tags() {
        return tags;
    }

    /** The current value. Arrays, hashes and structs are returned without copying. */
    public Value value() {
        return value;
    }

    /**
     * Replaces the value. The value is converted to the type of the variable (see {@link Values#convert}).
     * Constants are checked by the callers, which report their own errors.
     */
    public void set(Value newValue) {
        this.value = Values.convert(newValue, type);
    }
}
