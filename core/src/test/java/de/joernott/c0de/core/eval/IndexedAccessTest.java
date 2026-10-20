package de.joernott.c0de.core.eval;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.line.Block;
import de.joernott.c0de.core.line.Notation;
import de.joernott.c0de.core.program.LabelRef;
import de.joernott.c0de.core.value.ArrayValue;
import de.joernott.c0de.core.value.CharValue;
import de.joernott.c0de.core.value.FloatValue;
import de.joernott.c0de.core.value.HashValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.StructDefinition;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IndexedAccessTest {
    private static final StructDefinition POINT = new StructDefinition(
            "point",
            List.of(
                    new StructDefinition.Element("x", Type.Simple.FLOAT, Map.of()),
                    new StructDefinition.Element("tags", new Type.Array(new Type.Str(3L)), Map.of())));

    private MapScope scope;

    @BeforeEach
    void setUp() {
        ArrayValue row = new ArrayValue(Type.Simple.INTEGER, List.of(new IntegerValue(1), new IntegerValue(2)));
        ArrayValue matrix = new ArrayValue(new Type.Array(Type.Simple.INTEGER), List.of(row));
        HashValue inventory = new HashValue(Type.Simple.INTEGER, Map.of("diamond", new IntegerValue(3)));
        scope = new MapScope()
                .add(new Variable("matrix", matrix.type(), matrix, false, Map.of()))
                .add(new Variable("inventory", inventory.type(), inventory, false, Map.of()))
                .add(Variable.of("p", new Type.Struct(POINT)))
                .add(new Variable("s", Type.Str.UNLIMITED, new StringValue("héllo"), false, Map.of()))
                .add(new Variable("i", Type.Simple.INTEGER, new IntegerValue(1), false, Map.of()))
                .add(new Variable("key", Type.Str.UNLIMITED, new StringValue("diamond"), false, Map.of()))
                .add(Variable.of("error", Type.Struct.ERROR));
    }

    private static LabelRef label(String text) {
        Block.LabelBlock block = (Block.LabelBlock)
                Notation.parseLine(0, text, ZoneOffset.UTC).cells().get(0).block();
        return new LabelRef(block.name(), block.index());
    }

    private Value read(String label) {
        return IndexedAccess.read(label(label), scope);
    }

    private void write(String label, Value value) {
        IndexedAccess.write(label(label), scope, value);
    }

    private void assertCode(ErrorCode code, Runnable operation) {
        C0deException e = assertThrows(C0deException.class, operation::run);
        assertEquals(code, e.code(), e.getMessage());
    }

    @Test
    void reading() {
        assertEquals(new IntegerValue(2), read("matrix[0][1]"));
        assertEquals(new IntegerValue(2), read("matrix[0][i]"));
        assertEquals(new IntegerValue(3), read("inventory[\"diamond\"]"));
        assertEquals(new IntegerValue(3), read("inventory[key]"));
        assertEquals(new IntegerValue(3), read("inventory.diamond"));
        assertEquals(new FloatValue(0.0), read("p.x"));
        assertEquals(new IntegerValue(0), read("error.code"));
        assertEquals(new CharValue('é'), read("s[1]"));
        assertEquals(new CharValue('é'), read("s[i]"));
    }

    @Test
    void readingErrors() {
        assertCode(ErrorCode.INDEX_OUT_OF_BOUNDS, () -> read("matrix[1]"));
        assertCode(ErrorCode.INDEX_OUT_OF_BOUNDS, () -> read("matrix[0][-1]"));
        assertCode(ErrorCode.INDEX_OUT_OF_BOUNDS, () -> read("s[5]"));
        assertCode(ErrorCode.UNKNOWN_KEY, () -> read("inventory[\"gold\"]"));
        assertCode(ErrorCode.UNKNOWN_STRUCT_ENTITY, () -> read("p.y"));
        assertCode(ErrorCode.TYPE_MISMATCH, () -> read("matrix[\"a\"]"));
        assertCode(ErrorCode.TYPE_MISMATCH, () -> read("i[0]"));
        assertCode(ErrorCode.TYPE_MISMATCH, () -> read("p[0]"));
        assertCode(ErrorCode.UNKNOWN_NAME, () -> read("unknown[0]"));
        assertCode(ErrorCode.UNKNOWN_NAME, () -> read("matrix[j]"));
    }

    @Test
    void writing() {
        write("matrix[0][1]", new IntegerValue(5));
        assertEquals(new IntegerValue(5), read("matrix[0][1]"));
        // Index = length appends
        write("matrix[0][2]", new IntegerValue(6));
        assertEquals(3, ((ArrayValue) read("matrix[0]")).elements().size());
        write("inventory[\"gold\"]", new IntegerValue(1));
        assertEquals(new IntegerValue(1), read("inventory.gold"));
        // INTEGER into a FLOAT element
        write("p.x", new IntegerValue(2));
        assertEquals(new FloatValue(2.0), read("p.x"));
        write("p.tags[0]", new StringValue("abc"));
        assertEquals(new StringValue("abc"), read("p.tags[0]"));
        write("i", new IntegerValue(7));
        assertEquals(new IntegerValue(7), read("i"));
    }

    @Test
    void writingErrors() {
        assertCode(ErrorCode.INDEX_OUT_OF_BOUNDS, () -> write("matrix[0][3]", new IntegerValue(1)));
        assertCode(ErrorCode.TYPE_MISMATCH, () -> write("matrix[0][0]", new StringValue("x")));
        assertCode(ErrorCode.TYPE_MISMATCH, () -> write("s[0]", new CharValue('x')));
        assertCode(ErrorCode.STRING_TOO_LONG, () -> write("p.tags[0]", new StringValue("abcd")));
        assertCode(ErrorCode.UNKNOWN_STRUCT_ENTITY, () -> write("p.y", new IntegerValue(1)));
        assertCode(ErrorCode.TYPE_MISMATCH, () -> write("i", new FloatValue(1.5)));
    }

    @Test
    void assignedComplexValuesAreCopies() {
        Variable copy = Variable.of("copy", new Type.Array(new Type.Array(Type.Simple.INTEGER)));
        scope.add(copy);
        IndexedAccess.write(new LabelRef("copy"), scope, read("matrix"));
        write("matrix[0][0]", new IntegerValue(99));
        assertEquals(new IntegerValue(1), IndexedAccess.read(label("copy[0][0]"), scope));
    }
}
