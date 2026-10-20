package de.joernott.c0de.core.value;

import java.util.LinkedHashMap;
import java.util.stream.Collectors;

/** A value of a struct type (including ERROR). It is mutable; assignments copy it. */
public final class StructValue implements Value {
    private final StructDefinition definition;
    private final LinkedHashMap<String, Value> elements = new LinkedHashMap<>();

    /** A struct with the default values of all elements. */
    public StructValue(StructDefinition definition) {
        this.definition = definition;
        for (StructDefinition.Element element : definition.elements()) {
            elements.put(element.name(), element.type().defaultValue());
        }
    }

    public StructDefinition definition() {
        return definition;
    }

    /** The value of an element, or null if the struct has no such element. */
    public Value get(String element) {
        return elements.get(element);
    }

    /** Sets the value of an element. The caller has to check that the element exists and the type fits. */
    public void set(String element, Value value) {
        if (!elements.containsKey(element)) {
            throw new IllegalArgumentException("Struct " + definition.name() + " has no element " + element);
        }
        elements.put(element, value);
    }

    @Override
    public Type type() {
        return definition == StructDefinition.ERROR ? Type.Struct.ERROR : new Type.Struct(definition);
    }

    @Override
    public StructValue copy() {
        StructValue copy = new StructValue(definition);
        elements.forEach((key, value) -> copy.elements.put(key, value.copy()));
        return copy;
    }

    @Override
    public String text() {
        return elements.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue().text())
                .collect(Collectors.joining(", ", "{", "}"));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof StructValue struct
                && definition.equals(struct.definition)
                && elements.equals(struct.elements);
    }

    @Override
    public int hashCode() {
        return elements.hashCode();
    }

    @Override
    public String toString() {
        return definition.name() + text();
    }
}
