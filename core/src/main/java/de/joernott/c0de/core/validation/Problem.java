package de.joernott.c0de.core.validation;

import de.joernott.c0de.core.error.ErrorCode;

/**
 * A problem found by the static validation.
 *
 * @param line z coordinate of the line with the problem
 * @param code the error the cursor would raise or crash with
 * @param message the explanation in English
 */
public record Problem(int line, ErrorCode code, String message) {}
