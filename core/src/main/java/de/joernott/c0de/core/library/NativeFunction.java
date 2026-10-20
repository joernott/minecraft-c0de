package de.joernott.c0de.core.library;

import de.joernott.c0de.core.value.Value;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A function implemented in Java, e.g. a built-in function or a function of a library.
 *
 * @param name the name without library, e.g. {@code MID}
 * @param parameters the parameters
 * @param results the names of the results
 * @param implementation the code of the function
 */
public record NativeFunction(
        String name, List<Parameter> parameters, List<String> results, Implementation implementation) {
    public NativeFunction {
        parameters = List.copyOf(parameters);
        results = List.copyOf(results);
    }

    /** The parameter with this name. */
    public Optional<Parameter> parameter(String parameterName) {
        return parameters.stream().filter(p -> p.name().equals(parameterName)).findFirst();
    }

    /** The code of a native function. */
    @FunctionalInterface
    public interface Implementation {
        NativeResult call(NativeCall call);
    }

    /**
     * A parameter of a native function.
     *
     * @param name the name
     * @param types description of the accepted types for error messages, e.g. {@code INTEGER|FLOAT}
     * @param accepts the check of the value; a value that is not accepted raises TYPE_MISMATCH
     * @param defaultValue the default value, or null if the parameter is mandatory or optional without default
     * @param optional true if the parameter may be omitted
     * @param byReference true if the parameter must be a variable, which the function may change (the
     *     {@code destination} of FROM_JSON and FROM_YAML)
     */
    public record Parameter(
            String name,
            String types,
            Predicate<Value> accepts,
            Value defaultValue,
            boolean optional,
            boolean byReference) {
        /** A mandatory parameter. */
        public static Parameter required(String name, String types, Predicate<Value> accepts) {
            return new Parameter(name, types, accepts, null, false, false);
        }

        /** An optional parameter with a default value. */
        public static Parameter withDefault(String name, String types, Predicate<Value> accepts, Value defaultValue) {
            return new Parameter(name, types, accepts, defaultValue, true, false);
        }

        /** An optional parameter without default value; the function checks whether it was provided. */
        public static Parameter optional(String name, String types, Predicate<Value> accepts) {
            return new Parameter(name, types, accepts, null, true, false);
        }

        /** A mandatory parameter that is passed by reference. */
        public static Parameter reference(String name, String types) {
            return new Parameter(name, types, value -> true, null, false, true);
        }
    }
}
