package de.joernott.c0de.core.validation;

import de.joernott.c0de.core.error.ErrorCode;

/**
 * A problem found by the static validation.
 *
 * @param line z coordinate of the line with the problem
 * @param code the error the cursor would raise or crash with
 * @param message the explanation in English
 * @param incomplete true if the statement is not finished yet (e.g. BEGIN without END), see
 *     {@link de.joernott.c0de.core.error.C0deException#incomplete}
 */
public record Problem(int line, ErrorCode code, String message, boolean incomplete) {
    public Problem(int line, ErrorCode code, String message) {
        this(line, code, message, false);
    }
}
