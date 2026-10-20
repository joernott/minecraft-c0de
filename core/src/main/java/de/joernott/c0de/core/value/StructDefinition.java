package de.joernott.c0de.core.value;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** A struct type: its name and elements. */
public record StructDefinition(String name, List<Element> elements) {
    /** The predefined struct type ERROR. */
    public static final StructDefinition ERROR = new StructDefinition(
            "ERROR",
            List.of(
                    new Element("code", Type.Simple.INTEGER, Map.of()),
                    new Element("message", Type.Str.UNLIMITED, Map.of()),
                    new Element("context", Type.Str.UNLIMITED, Map.of()),
                    new Element("line", Type.Simple.INTEGER, Map.of())));

    public StructDefinition {
        elements = List.copyOf(elements);
    }

    /** The element with this name. */
    public Optional<Element> element(String name) {
        return elements.stream().filter(e -> e.name().equals(name)).findFirst();
    }

    /**
     * An element of a struct.
     *
     * @param tags the tags of the element: language ("json", "yaml") to key name
     */
    public record Element(String name, Type type, Map<String, String> tags) {
        public Element {
            tags = Map.copyOf(tags);
        }
    }
}
