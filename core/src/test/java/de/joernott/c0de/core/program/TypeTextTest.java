package de.joernott.c0de.core.program;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.joernott.c0de.core.library.standard.JsonValues;
import de.joernott.c0de.core.value.ArrayValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import java.util.List;
import org.junit.jupiter.api.Test;

/** The text form of types and the JSON form of values, as stored in variable and constant items. */
class TypeTextTest {
    @Test
    void roundTrip() {
        for (String text : List.of(
                "INTEGER", "STRING", "STRING 20", "ARRAY HASH FLOAT", "STRUCT point", "ERROR", "HASH STRUCT a/b")) {
            assertEquals(text, TypeText.format(TypeText.parse(text)));
        }
        assertEquals("ARRAY INTEGER", TypeText.format(TypeText.parse(" array  integer ")));
    }

    @Test
    void incompleteTypes() {
        assertThrows(IllegalArgumentException.class, () -> TypeText.parse("ARRAY"));
        assertThrows(IllegalArgumentException.class, () -> TypeText.parse("STRUCT"));
        assertThrows(IllegalArgumentException.class, () -> TypeText.parse("INTEGER INTEGER"));
        assertThrows(IllegalArgumentException.class, () -> TypeText.parse("NUMBER"));
        assertThrows(IllegalArgumentException.class, () -> TypeText.parse("ARRAY ".repeat(9) + "INTEGER"));
    }

    @Test
    void jsonValues() {
        Type type = new Type.Array(Type.Simple.INTEGER);
        Value value = JsonValues.read("[1, 2]", type);
        assertEquals(new ArrayValue(Type.Simple.INTEGER, List.of(new IntegerValue(1), new IntegerValue(2))), value);
        assertEquals("[1,2]", JsonValues.write(value));
    }
}
