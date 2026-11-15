package de.joernott.c0de.core.library;

import de.joernott.c0de.core.value.Literals;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import java.time.ZoneOffset;

/**
 * The text form of secrets in the persistent vault (docs/concept/20-lib-security.md): the type as its keyword and
 * the value in the notation of a LITERAL block, as the client stores them in the KeePass file.
 */
public final class SecretText {
    /** The longest value of a secret, in characters. */
    public static final int MAX_LENGTH = 2000;

    private SecretText() {}

    /** The type of a value as keyword: BOOLEAN, INTEGER, FLOAT, CHAR, STRING or TIME. */
    public static String type(Value value) {
        Type type = value.type();
        return type instanceof Type.Str ? "STRING" : type.toString();
    }

    /** The value as text, read back by {@link #parse}. */
    public static String text(Value value) {
        return value.text();
    }

    /**
     * Reads a value of a secret.
     *
     * @throws IllegalArgumentException if the type is not one of a secret or the text doesn't fit it
     */
    public static Value parse(String type, String text) {
        if (text.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("A secret has at most " + MAX_LENGTH + " characters");
        }
        for (Type candidate : Literals.TYPES) {
            String name = candidate instanceof Type.Str ? "STRING" : candidate.toString();
            if (name.equals(type)) {
                return candidate instanceof Type.Str
                        ? new StringValue(text)
                        : Literals.parse(candidate, text, ZoneOffset.UTC);
            }
        }
        throw new IllegalArgumentException("A secret can't have the type " + type);
    }
}
