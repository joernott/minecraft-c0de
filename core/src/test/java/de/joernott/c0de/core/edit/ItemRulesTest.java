package de.joernott.c0de.core.edit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

/** The properties of variable and constant items (task 8.1). */
class ItemRulesTest {
    private static ItemRules.Result check(String name, String type, String value) {
        return ItemRules.check(name, type, value, ZoneOffset.UTC);
    }

    @Test
    void valuesAreStoredAsJson() {
        assertEquals("42", check("count", "INTEGER", "42").json());
        assertEquals("\"Hello World\"", check("text", "STRING", "Hello World").json());
        assertEquals("[1,2]", check("list", "ARRAY INTEGER", "[1, 2]").json());
        assertEquals("{\"x\": 1}", check("p", "STRUCT point", "{\"x\": 1}").json());
        assertEquals("", check("", "INTEGER", "").json(), "an empty item");
        assertTrue(check("", "", "").problem().isEmpty(), "an uninitialized item");
        assertTrue(check("", "  ", "").problem().isEmpty(), "an uninitialized item with spaces");
    }

    @Test
    void typeChoices() {
        List<String> all = ItemRules.typeChoices("", List.of("c0de/minecraft/POSITION"));
        assertTrue(all.containsAll(List.of(
                "BOOLEAN",
                "STRUCT c0de/minecraft/POSITION",
                "ARRAY INTEGER",
                "HASH STRING",
                "ARRAY STRUCT c0de/minecraft/POSITION")));
        // Every choice is a valid type
        all.forEach(type -> assertTrue(check("x", type, "").problem().isEmpty(), type));
        // A crafted item keeps its kind of type
        assertTrue(ItemRules.typeChoices("ARRAY", List.of()).stream().allMatch(type -> type.startsWith("ARRAY ")));
    }

    @Test
    void invalidProperties() {
        assertTrue(check("count", "INTEGER", "4.2").problem().isPresent());
        assertTrue(check("", "INTEGER", "1").problem().isPresent(), "a value without a name");
        assertTrue(check("1x", "INTEGER", "").problem().isPresent());
        assertTrue(check("list", "ARRAY", "").problem().isPresent(), "the subtype is missing");
        assertTrue(check("list", "ARRAY INTEGER", "[\"a\"]").problem().isPresent());
        assertTrue(check("p", "STRUCT point", "[1]").problem().isPresent());
    }

    @Test
    void display() {
        assertEquals("Hello World", ItemRules.display("STRING", "\"Hello World\""));
        assertEquals("42", ItemRules.display("INTEGER", "42"));
        assertEquals("[1,2]", ItemRules.display("ARRAY INTEGER", "[1,2]"));
    }
}
