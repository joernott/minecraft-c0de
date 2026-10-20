package de.joernott.c0de.core.value;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** A HASH value with STRING keys, sorted by key. It is mutable; assignments copy it. */
public final class HashValue implements Value {
    private final Type elementType;
    private final TreeMap<String, Value> elements = new TreeMap<>();

    public HashValue(Type elementType) {
        this.elementType = elementType;
    }

    public HashValue(Type elementType, Map<String, ? extends Value> elements) {
        this(elementType);
        this.elements.putAll(elements);
    }

    public Type elementType() {
        return elementType;
    }

    /** The elements, sorted by key. The map is mutable. */
    public TreeMap<String, Value> elements() {
        return elements;
    }

    @Override
    public Type type() {
        return new Type.Hash(elementType);
    }

    @Override
    public HashValue copy() {
        HashValue copy = new HashValue(elementType);
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
        return other instanceof HashValue hash
                && elementType.equals(hash.elementType)
                && elements.equals(hash.elements);
    }

    @Override
    public int hashCode() {
        return elements.hashCode();
    }

    @Override
    public String toString() {
        return "HashValue" + text();
    }
}
