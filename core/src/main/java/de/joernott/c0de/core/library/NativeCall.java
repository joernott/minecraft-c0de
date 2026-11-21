package de.joernott.c0de.core.library;

import de.joernott.c0de.core.eval.Variable;
import de.joernott.c0de.core.value.Value;
import java.util.Map;
import java.util.Optional;

/** The arguments of a call of a {@link NativeFunction}. */
public interface NativeCall {
    /** The value of a parameter: the provided value or the default value. Null if neither exists. */
    Value argument(String name);

    /** True if the parameter was provided in the CALL statement. */
    boolean provided(String name);

    /**
     * The tags of the variable or constant that provided a parameter. They are passed if the PARAMETER
     * expression is a single LABEL without index; otherwise the map is empty.
     */
    Map<String, String> tags(String name);

    /**
     * The name of the variable or constant that provided a parameter, if the PARAMETER expression is a single
     * LABEL without index (WRITE names its item after it).
     */
    default Optional<String> label(String name) {
        return Optional.empty();
    }

    /**
     * True if the parameter was provided by a single LABEL without index whose variable or constant is sensitive
     * (docs/concept/20-lib-security.md): WRITE makes a hidden item of it.
     */
    default boolean sensitive(String name) {
        return false;
    }

    /** The variable passed to a by-reference parameter. */
    Optional<Variable> reference(String name);

    /** The process that calls the function. */
    ProcessContext process();

    /**
     * The progress of a function that does its work in parts (since c0de 0.7.0): the object last given to
     * {@link #setState} while the cursor stayed on this CALL line, returning {@link NativeResult#waitForValue()}.
     * Null on the first call, after the function returned anything else, and after a restart of the server, as
     * the state isn't saved.
     */
    default Object state() {
        return null;
    }

    /** Keeps the progress of the function for its next call on the same CALL line (see {@link #state()}). */
    default void setState(Object state) {}
}
