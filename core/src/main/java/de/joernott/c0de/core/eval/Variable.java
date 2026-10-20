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
    /** Value of {@link #line()} for variables that are not declared in a line, e.g. in tests. */
    public static final int NO_LINE = -1;

    private final String name;
    private final Type type;
    private final boolean constant;
    private final Map<String, String> tags;
    private final int line;
    private final boolean loopVariable;
    private Value value;

    public Variable(String name, Type type, Value value, boolean constant, Map<String, String> tags) {
        this(name, type, value, constant, tags, NO_LINE, false);
    }

    /**
     * @param line z coordinate of the declaration (for the visibility of global variables)
     * @param loopVariable true for the variables of FOR and EACH loops, which can't be assigned
     */
    public Variable(
            String name,
            Type type,
            Value value,
            boolean constant,
            Map<String, String> tags,
            int line,
            boolean loopVariable) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.constant = constant;
        this.tags = Map.copyOf(tags);
        this.line = line;
        this.loopVariable = loopVariable;
    }

    /** A variable with the default value of its type and no tags. */
    public static Variable of(String name, Type type) {
        return new Variable(name, type, type.defaultValue(), false, Map.of());
    }

    /** The same variable, declared in a line. */
    public Variable inLine(int declarationLine) {
        return new Variable(name, type, value, constant, tags, declarationLine, loopVariable);
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

    /** The z coordinate of the declaration, or {@link #NO_LINE}. */
    public int line() {
        return line;
    }

    /** True for the variables of FOR and EACH loops. */
    public boolean loopVariable() {
        return loopVariable;
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
