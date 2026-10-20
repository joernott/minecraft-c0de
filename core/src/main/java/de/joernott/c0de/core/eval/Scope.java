package de.joernott.c0de.core.eval;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import java.util.Optional;

/** Resolves names of variables and constants. */
public interface Scope {
    /** The variable or constant with this name, if it is visible. */
    Optional<Variable> find(String name);

    /**
     * The variable or constant with this name.
     *
     * @throws C0deException UNKNOWN_NAME if it is not visible
     */
    default Variable resolve(String name) {
        return find(name)
                .orElseThrow(
                        () -> C0deException.of(ErrorCode.UNKNOWN_NAME, "There is no variable or constant " + name));
    }
}
