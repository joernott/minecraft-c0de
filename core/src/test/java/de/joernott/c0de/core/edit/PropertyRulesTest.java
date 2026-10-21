package de.joernott.c0de.core.edit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0de.core.line.IndexEntry;
import de.joernott.c0de.core.line.IndexText;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.TimeValue;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** The checks of the dialog input. */
class PropertyRulesTest {
    private static boolean validLabel(LabelMode mode, String name, String index) {
        return PropertyRules.label(mode, name, index).isEmpty();
    }

    @Test
    void labelNamesDependOnThePosition() {
        assertTrue(validLabel(LabelMode.NEW_NAME, "result", ""));
        assertFalse(validLabel(LabelMode.NEW_NAME, "RESULT", ""));
        assertEquals(Optional.of("RESULT is a keyword"), PropertyRules.label(LabelMode.NEW_NAME, "RESULT", ""));
        assertFalse(validLabel(LabelMode.NEW_NAME, "", ""));
        assertFalse(validLabel(LabelMode.NEW_NAME, "1x", ""));
        assertFalse(validLabel(LabelMode.NEW_NAME, "c0de/math", ""));
        assertTrue(validLabel(LabelMode.LIBRARY, "c0de/math", ""));
        assertFalse(validLabel(LabelMode.LIBRARY, "math", ""));
        assertTrue(validLabel(LabelMode.FUNCTION, "c0de/math/SIN", ""));
        assertTrue(validLabel(LabelMode.FUNCTION, "main", ""));
        assertTrue(validLabel(LabelMode.REFERENCE, "c0de/math/PI", ""));
        assertTrue(validLabel(LabelMode.STRUCT_TYPE, "c0de/minecraft/POSITION", ""));
        assertTrue(validLabel(LabelMode.STRUCT_TYPE, "point", ""));
    }

    @Test
    void onlyReferencesHaveAnIndex() {
        assertTrue(validLabel(LabelMode.REFERENCE, "matrix", "[i][2]"));
        assertTrue(validLabel(LabelMode.REFERENCE, "inventory", "[\"diamond\"]"));
        assertTrue(validLabel(LabelMode.REFERENCE, "problem", ".code"));
        assertFalse(validLabel(LabelMode.NEW_NAME, "matrix", "[1]"));
        assertFalse(validLabel(LabelMode.REFERENCE, "matrix", "[1"));
        assertFalse(validLabel(LabelMode.REFERENCE, "matrix", "[END]"));
        assertFalse(validLabel(LabelMode.REFERENCE, "matrix", "x"));
        assertTrue(validLabel(LabelMode.REFERENCE, "m", "[0]".repeat(8)));
        assertFalse(validLabel(LabelMode.REFERENCE, "m", "[0]".repeat(9)));
    }

    @Test
    void indexTextRoundTrip() {
        List<IndexEntry> index = PropertyRules.index("[i][2][\"a\\\"b\"].x").entries();
        assertEquals(
                List.of(
                        new IndexEntry.Variable("i"),
                        new IndexEntry.Literal(new IntegerValue(2)),
                        new IndexEntry.Literal(new StringValue("a\"b")),
                        new IndexEntry.Element("x")),
                index);
        assertEquals("[i][2][\"a\\\"b\"].x", IndexText.format(index));
    }

    @Test
    void functionsAreSplitIntoParentAndName() {
        Set<String> builtins = Set.of("LEN");
        assertEquals(List.of("c0de/math", "SIN"), PropertyRules.splitFunction("c0de/math/SIN", builtins, "demo"));
        assertEquals(List.of("", "LEN"), PropertyRules.splitFunction("LEN", builtins, "demo"));
        assertEquals(List.of("demo", "main"), PropertyRules.splitFunction("main", builtins, "demo"));
    }

    @Test
    void literals() {
        ZoneId berlin = ZoneId.of("Europe/Berlin");
        assertTrue(PropertyRules.literal("INTEGER", "42", berlin).isEmpty());
        assertFalse(PropertyRules.literal("INTEGER", "4.2", berlin).isEmpty());
        assertTrue(PropertyRules.literal("STRING", "", berlin).isEmpty());
        assertFalse(PropertyRules.literal("CHAR", "ab", berlin).isEmpty());
        assertFalse(PropertyRules.literal("ARRAY", "1", berlin).isEmpty());
        assertEquals(Optional.of(new TimeValue(0)), PropertyRules.literalValue("TIME", "1970-01-01T01:00:00", berlin));
        assertEquals(
                Optional.of(new TimeValue(0)), PropertyRules.literalValue("TIME", "1970-01-01T00:00Z", ZoneOffset.UTC));
    }

    @Test
    void tags() {
        assertTrue(PropertyRules.tag("json", "server.port", true).isEmpty());
        assertFalse(PropertyRules.tag("json", "server..port", true).isEmpty());
        assertTrue(PropertyRules.tag("yaml", "-", false).isEmpty());
        assertFalse(PropertyRules.tag("yaml", "a.b", false).isEmpty());
        assertFalse(PropertyRules.tag("xml", "a", false).isEmpty());
        assertFalse(PropertyRules.tag("json", "", false).isEmpty());
    }

    @Test
    void comments() {
        assertTrue(PropertyRules.comment("").isEmpty());
        assertFalse(PropertyRules.comment("x".repeat(PropertyRules.MAX_TEXT_LENGTH + 1))
                .isEmpty());
    }
}
