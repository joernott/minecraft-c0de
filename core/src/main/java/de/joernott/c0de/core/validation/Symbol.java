package de.joernott.c0de.core.validation;

import de.joernott.c0de.core.value.Type;

/**
 * A variable or constant that is visible in a line.
 *
 * @param name the name, qualified for constants of libraries
 * @param kind what the name was declared as
 * @param type the type, or null if it is only known at runtime (the variable of an EACH loop)
 */
public record Symbol(String name, Kind kind, Type type) {
    /** How a name was declared. */
    public enum Kind {
        VARIABLE,
        CONSTANT,
        PARAMETER,
        RESULT,
        LOOP_VARIABLE
    }

    /** True if the value can be changed by ASSIGN, CALL results and loops. */
    public boolean isWritable() {
        return kind == Kind.VARIABLE || kind == Kind.PARAMETER || kind == Kind.RESULT;
    }
}
