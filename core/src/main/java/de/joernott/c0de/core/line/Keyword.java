package de.joernott.c0de.core.line;

import de.joernott.c0de.core.value.Type;
import java.util.Optional;

/** The keyword blocks: statement keywords and type keywords. */
public enum Keyword {
    NOOP,
    PROGRAM,
    INCLUDE,
    EXIT,
    BEGIN,
    END,
    BREAK,
    VARIABLE,
    CONSTANT,
    ASSIGN,
    FUNCTION,
    PARAMETER,
    RESULT,
    RETURN,
    CALL,
    IF,
    ELSE,
    FOR,
    EACH,
    WHILE,
    REPEAT,
    // Types. STRUCT is also the keyword of a struct declaration.
    BOOLEAN,
    INTEGER,
    FLOAT,
    CHAR,
    TIME,
    STRING,
    ARRAY,
    HASH,
    STRUCT,
    ERROR;

    /** True for the type keywords. */
    public boolean isType() {
        return ordinal() >= BOOLEAN.ordinal();
    }

    /** The simple type of a type keyword, if it is one. */
    public Optional<Type.Simple> simpleType() {
        return switch (this) {
            case BOOLEAN -> Optional.of(Type.Simple.BOOLEAN);
            case INTEGER -> Optional.of(Type.Simple.INTEGER);
            case FLOAT -> Optional.of(Type.Simple.FLOAT);
            case CHAR -> Optional.of(Type.Simple.CHAR);
            case TIME -> Optional.of(Type.Simple.TIME);
            default -> Optional.empty();
        };
    }

    /**
     * True if the word is reserved and can't be used as a name: keywords, the names of the other blocks and
     * the logical operators. Only the capitalized spelling is reserved, so {@code result} is a valid name.
     */
    public static boolean isReserved(String word) {
        for (Keyword keyword : values()) {
            if (keyword.name().equals(word)) {
                return true;
            }
        }
        return switch (word) {
            case "LABEL", "LITERAL", "OPERATOR", "COMMENT", "TAG", "NOT", "AND", "OR", "XOR", "TRUE", "FALSE" -> true;
            default -> false;
        };
    }
}
