package de.joernott.c0de.core.eval;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** A simple scope with a map of variables, e.g. for tests. */
public final class MapScope implements Scope {
    private final Map<String, Variable> variables = new LinkedHashMap<>();

    /** Adds a variable or constant. */
    public MapScope add(Variable variable) {
        variables.put(variable.name(), variable);
        return this;
    }

    @Override
    public Optional<Variable> find(String name) {
        return Optional.ofNullable(variables.get(name));
    }
}
