package de.joernott.c0de.core.value;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** An ARRAY value. It is mutable; assignments copy it. */
public final class ArrayValue implements Value {
    private final Type elementType;
    private final List<Value> elements = new ArrayList<>();

    public ArrayValue(Type elementType) {
        this.elementType = elementType;
    }

    public ArrayValue(Type elementType, List<? extends Value> elements) {
        this(elementType);
        this.elements.addAll(elements);
    }

    public Type elementType() {
        return elementType;
    }

    /** The elements. The list is mutable. */
    public List<Value> elements() {
        return elements;
    }

    @Override
    public Type type() {
        return new Type.Array(elementType);
    }

    @Override
    public ArrayValue copy() {
        return new ArrayValue(elementType, elements.stream().map(Value::copy).toList());
    }

    @Override
    public String text() {
        return elements.stream().map(Value::text).collect(Collectors.joining(", ", "[", "]"));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ArrayValue array
                && elementType.equals(array.elementType)
                && elements.equals(array.elements);
    }

    @Override
    public int hashCode() {
        return elements.hashCode();
    }

    @Override
    public String toString() {
        return "ArrayValue" + text();
    }
}
