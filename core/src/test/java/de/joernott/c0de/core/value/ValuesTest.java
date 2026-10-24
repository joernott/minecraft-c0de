package de.joernott.c0de.core.value;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ValuesTest {
    private static final StructDefinition POINT = new StructDefinition(
            "point",
            List.of(
                    new StructDefinition.Element("x", Type.Simple.INTEGER, Map.of()),
                    new StructDefinition.Element("label", new Type.Str(5L), Map.of())));

    @Test
    void defaultValues() {
        assertEquals(BooleanValue.FALSE, Type.Simple.BOOLEAN.defaultValue());
        assertEquals(new IntegerValue(0), Type.Simple.INTEGER.defaultValue());
        assertEquals(new FloatValue(0.0), Type.Simple.FLOAT.defaultValue());
        assertEquals(new CharValue(' '), Type.Simple.CHAR.defaultValue());
        assertEquals(new TimeValue(0), Type.Simple.TIME.defaultValue());
        assertEquals(new StringValue(""), Type.Str.UNLIMITED.defaultValue());
        assertEquals(new ArrayValue(Type.Simple.INTEGER), new Type.Array(Type.Simple.INTEGER).defaultValue());
        StructValue point = (StructValue) new Type.Struct(POINT).defaultValue();
        assertEquals(new IntegerValue(0), point.get("x"));
        assertEquals(new StringValue(""), point.get("label"));
        StructValue error = (StructValue) Type.Struct.ERROR.defaultValue();
        assertEquals(new IntegerValue(0), error.get("code"));
        assertEquals(Type.Struct.ERROR, error.type());
    }

    @Test
    void textRepresentation() {
        assertEquals("true", BooleanValue.TRUE.text());
        assertEquals("-42", new IntegerValue(-42).text());
        assertEquals("2.5", new FloatValue(2.5).text());
        assertEquals("ä", new CharValue('ä').text());
        assertEquals("2026-10-19T12:05:09.000Z", new TimeValue(1792411509000L).text());
        assertEquals("2026-10-19T12:05:09.250Z", new TimeValue(1792411509250L).text());
        ArrayValue array = new ArrayValue(Type.Simple.INTEGER, List.of(new IntegerValue(1), new IntegerValue(2)));
        assertEquals("[1, 2]", array.text());
        HashValue hash = new HashValue(Type.Simple.INTEGER, Map.of("b", new IntegerValue(2), "a", new IntegerValue(1)));
        assertEquals("{a: 1, b: 2}", hash.text());
        assertEquals("{x: 0, label: }", new StructValue(POINT).text());
    }

    @Test
    void copiesAreDeep() {
        ArrayValue inner = new ArrayValue(Type.Simple.INTEGER, List.of(new IntegerValue(1)));
        ArrayValue outer = new ArrayValue(new Type.Array(Type.Simple.INTEGER), List.of(inner));
        ArrayValue copy = outer.copy();
        assertEquals(outer, copy);
        ((ArrayValue) copy.elements().get(0)).elements().set(0, new IntegerValue(9));
        assertEquals(new IntegerValue(1), inner.elements().get(0), "The original is not changed");
        assertNotSame(outer, Values.convert(outer, outer.type()), "Converting copies complex values");
    }

    @Test
    void conversionFollowsTheAssignRules() {
        assertEquals(new FloatValue(3.0), Values.convert(new IntegerValue(3), Type.Simple.FLOAT));
        assertEquals(new StringValue("a"), Values.convert(new CharValue('a'), Type.Str.UNLIMITED));
        assertEquals(new StringValue("abc"), Values.convert(new StringValue("abc"), new Type.Str(3L)));
        assertCode(ErrorCode.STRING_TOO_LONG, () -> Values.convert(new StringValue("abcd"), new Type.Str(3L)));
        assertCode(ErrorCode.TYPE_MISMATCH, () -> Values.convert(new FloatValue(3.0), Type.Simple.INTEGER));
        assertCode(ErrorCode.TYPE_MISMATCH, () -> Values.convert(new StringValue("a"), Type.Simple.CHAR));
        assertCode(ErrorCode.TYPE_MISMATCH, () -> Values.convert(new IntegerValue(1), Type.Simple.TIME));
        ArrayValue integers = new ArrayValue(Type.Simple.INTEGER, List.of(new IntegerValue(1)));
        assertEquals(
                new ArrayValue(Type.Simple.FLOAT, List.of(new FloatValue(1.0))),
                Values.convert(integers, new Type.Array(Type.Simple.FLOAT)));
        assertCode(ErrorCode.TYPE_MISMATCH, () -> Values.convert(integers, new Type.Hash(Type.Simple.INTEGER)));
        StructDefinition other = new StructDefinition("other", POINT.elements());
        assertCode(ErrorCode.TYPE_MISMATCH, () -> Values.convert(new StructValue(POINT), new Type.Struct(other)));
        assertTrue(Values.fits(new StringValue("too long"), new Type.Str(1L)), "fits ignores length limits");
        assertFalse(Values.fits(new StringValue("x"), Type.Simple.INTEGER));
    }

    @Test
    void equalityAndOrder() {
        assertTrue(Values.equal(new IntegerValue(2), new FloatValue(2.0)));
        assertTrue(Values.equal(new CharValue('x'), new StringValue("x")));
        assertTrue(Values.equal(new StructValue(POINT), new StructValue(POINT)));
        assertCode(ErrorCode.TYPE_MISMATCH, () -> Values.equal(BooleanValue.TRUE, new IntegerValue(1)));
        assertTrue(Values.compare(new StringValue("Z"), new StringValue("a")) < 0, "by code point");
        assertTrue(Values.compare(new TimeValue(1), new TimeValue(2)) < 0);
        assertCode(ErrorCode.TYPE_MISMATCH, () -> Values.compare(new TimeValue(1), new IntegerValue(2)));
    }

    @Test
    void literals() {
        ZoneId berlin = ZoneId.of("Europe/Berlin");
        assertEquals(BooleanValue.TRUE, Literals.parse(Type.Simple.BOOLEAN, "True", berlin));
        assertEquals(new IntegerValue(-7), Literals.parse(Type.Simple.INTEGER, " -7 ", berlin));
        assertEquals(new FloatValue(1.5e3), Literals.parse(Type.Simple.FLOAT, "1.5e3", berlin));
        assertEquals(new CharValue(0x1F600), Literals.parse(Type.Simple.CHAR, "😀", berlin));
        assertEquals(new CharValue(' '), Literals.parse(Type.Simple.CHAR, " ", berlin));
        assertEquals(new StringValue(" spaces "), Literals.parse(Type.Str.UNLIMITED, " spaces ", berlin));
        assertEquals(
                new TimeValue(1792411509250L), Literals.parse(Type.Simple.TIME, "2026-10-19T12:05:09.250Z", berlin));
        assertEquals(
                new TimeValue(1792411509000L), Literals.parse(Type.Simple.TIME, "2026-10-19T14:05:09+02:00", berlin));
        // Without offset: the time zone of the server (CEST in October)
        assertEquals(new TimeValue(1792411509000L), Literals.parse(Type.Simple.TIME, "2026-10-19T14:05:09", berlin));
        assertEquals(
                new TimeValue(1792411509000L), Literals.parse(Type.Simple.TIME, "2026-10-19T12:05:09", ZoneOffset.UTC));
        for (String invalid : List.of("yes", "")) {
            assertThrows(IllegalArgumentException.class, () -> Literals.parse(Type.Simple.BOOLEAN, invalid, berlin));
        }
        assertThrows(IllegalArgumentException.class, () -> Literals.parse(Type.Simple.INTEGER, "1.5", berlin));
        assertThrows(IllegalArgumentException.class, () -> Literals.parse(Type.Simple.FLOAT, "NaN", berlin));
        assertThrows(IllegalArgumentException.class, () -> Literals.parse(Type.Simple.CHAR, "ab", berlin));
        assertThrows(IllegalArgumentException.class, () -> Literals.parse(Type.Simple.TIME, "yesterday", berlin));
        assertThrows(
                IllegalArgumentException.class, () -> Literals.parse(new Type.Array(Type.Simple.INTEGER), "1", berlin));
    }

    private static void assertCode(ErrorCode code, Runnable operation) {
        C0deException e = assertThrows(C0deException.class, operation::run);
        assertEquals(code, e.code(), e.getMessage());
    }
}
