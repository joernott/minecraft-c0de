package de.joernott.c0de.core.program;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The languages of TAG blocks: {@code json} and {@code yaml} name keys and paths (docs/concept/04-data.md), and
 * {@code security} marks sensitive values with its only name {@code sensitive}
 * (docs/concept/20-lib-security.md).
 */
public final class Tags {
    public static final String JSON = "json";
    public static final String YAML = "yaml";
    public static final String SECURITY = "security";
    /** The only name of the language {@link #SECURITY}. */
    public static final String SENSITIVE = "sensitive";
    /** The languages in the order of the dialog of the TAG block. */
    public static final List<String> LANGUAGES = List.of(JSON, YAML, SECURITY);

    private Tags() {}

    /** True if the tags mark a value as sensitive. */
    public static boolean sensitive(Map<String, String> tags) {
        return SENSITIVE.equals(tags.get(SECURITY));
    }

    /**
     * Checks the language and name of a TAG.
     *
     * @param path true for the tag of a variable or constant (a path of keys separated by dots), false for the
     *     tag of a struct element (a single key)
     * @param maxLength the maximum length of the name
     * @return the problem, or empty if the tag is valid
     */
    public static Optional<String> check(String language, String name, boolean path, int maxLength) {
        if (language == null || !LANGUAGES.contains(language)) {
            return Optional.of("The language of a tag is json, yaml or security");
        }
        if (name == null || name.isEmpty()) {
            return Optional.of("The name is missing");
        }
        if (language.equals(SECURITY)) {
            return name.equals(SENSITIVE)
                    ? Optional.empty()
                    : Optional.of("The only name of a security tag is " + SENSITIVE);
        }
        if (name.length() > maxLength) {
            return Optional.of("The name is too long");
        }
        if (path) {
            for (String key : name.split("\\.", -1)) {
                if (key.isEmpty()) {
                    return Optional.of("A key of the path is empty");
                }
            }
        } else if (name.contains(".")) {
            return Optional.of("The tag of a struct element contains no dot");
        }
        return Optional.empty();
    }
}
