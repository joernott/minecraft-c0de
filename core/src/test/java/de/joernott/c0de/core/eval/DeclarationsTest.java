package de.joernott.c0de.core.eval;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.line.Notation;
import de.joernott.c0de.core.program.Program;
import de.joernott.c0de.core.program.ProgramParser;
import de.joernott.c0de.core.program.Statement;
import de.joernott.c0de.core.value.ArrayValue;
import de.joernott.c0de.core.value.FloatValue;
import de.joernott.c0de.core.value.HashValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.StructDefinition;
import de.joernott.c0de.core.value.StructValue;
import de.joernott.c0de.core.value.Type;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DeclarationsTest {
    /** Executes the global declarations of a program and returns the variables by name. */
    private static MapScope run(String declarations) {
        Program program = ProgramParser.parse(Notation.parse("PROGRAM demo\nNOOP\n" + declarations));
        assertTrue(program.errors().isEmpty(), "errors: " + program.errors());
        TypeRegistry types = new TypeRegistry();
        MapScope scope = new MapScope();
        for (Statement statement : program.statements()) {
            switch (statement) {
                case Statement.StructDeclaration struct ->
                    types.add(struct.name(), Declarations.defineStruct(struct, types));
                case Statement.Declaration declaration -> scope.add(Declarations.declare(declaration, types));
                default -> throw new IllegalArgumentException("Unexpected statement " + statement);
            }
        }
        return scope;
    }

    private static void assertCrash(ErrorCode code, int line, String declarations) {
        C0deException e = assertThrows(C0deException.class, () -> run(declarations));
        assertEquals(code, e.code(), e.getMessage());
        assertEquals(line, e.line(), e.toString());
        assertTrue(e.crash(), "Errors in declarations make the cursor crash");
    }

    @Test
    void simpleTypes() {
        MapScope scope = run("""
                VARIABLE a INTEGER
                VARIABLE b FLOAT 2
                CONSTANT c STRING 3 "abc"
                VARIABLE d STRING "x" TAG(json: "server.name")
                VARIABLE e CHAR 'q'
                """);
        assertEquals(new IntegerValue(0), scope.resolve("a").value());
        assertEquals(new FloatValue(2.0), scope.resolve("b").value(), "An INTEGER literal fits into a FLOAT");
        Variable c = scope.resolve("c");
        assertTrue(c.constant());
        assertEquals(new Type.Str(3L), c.type());
        assertEquals(Map.of("json", "server.name"), scope.resolve("d").tags());
    }

    @Test
    void literalErrors() {
        assertCrash(ErrorCode.SYNTAX_ERROR_TYPE_MISMATCH, 2, "VARIABLE a INTEGER 1.5\n");
        assertCrash(ErrorCode.SYNTAX_ERROR_TYPE_MISMATCH, 2, "VARIABLE a BOOLEAN \"true\"\n");
        assertCrash(ErrorCode.STRING_TOO_LONG, 2, "VARIABLE a STRING 2 \"abc\"\n");
    }

    @Test
    void structsAndInitializers() {
        MapScope scope = run("""
                STRUCT point
                BEGIN
                    x INTEGER
                    y INTEGER TAG(json: "py")
                END STRUCT
                STRUCT line
                BEGIN
                    from STRUCT point
                    to STRUCT point
                END STRUCT
                VARIABLE l STRUCT line
                BEGIN
                    to
                    BEGIN
                        x 5
                    END
                END VARIABLE
                CONSTANT primes ARRAY INTEGER
                BEGIN
                    2 3 5
                    7
                END CONSTANT
                VARIABLE grid ARRAY ARRAY FLOAT
                BEGIN
                    BEGIN
                        1 2.5
                    END
                    BEGIN
                        3
                    END
                END VARIABLE
                VARIABLE stock HASH INTEGER
                BEGIN
                    diamond 3
                    gold 10
                END VARIABLE
                """);
        StructValue l = (StructValue) scope.resolve("l").value();
        assertEquals(new IntegerValue(5), ((StructValue) l.get("to")).get("x"));
        assertEquals(new IntegerValue(0), ((StructValue) l.get("from")).get("x"), "Missing elements keep defaults");
        StructDefinition point = ((StructValue) l.get("to")).definition();
        assertEquals(Map.of("json", "py"), point.element("y").orElseThrow().tags());

        ArrayValue primes = (ArrayValue) scope.resolve("primes").value();
        assertEquals(
                List.of(new IntegerValue(2), new IntegerValue(3), new IntegerValue(5), new IntegerValue(7)),
                primes.elements());
        ArrayValue grid = (ArrayValue) scope.resolve("grid").value();
        assertEquals(
                new FloatValue(1.0),
                ((ArrayValue) grid.elements().get(0)).elements().get(0));
        assertEquals(1, ((ArrayValue) grid.elements().get(1)).elements().size());
        HashValue stock = (HashValue) scope.resolve("stock").value();
        assertEquals(new IntegerValue(10), stock.elements().get("gold"));
    }

    @Test
    void declarationErrors() {
        assertCrash(ErrorCode.UNKNOWN_STRUCT_ENTITY, 2, "VARIABLE p STRUCT point\n");
        assertCrash(ErrorCode.UNKNOWN_STRUCT_ENTITY, 4, "STRUCT a\nBEGIN\n    b STRUCT unknown\nEND STRUCT\n");
        String point = "STRUCT point\nBEGIN\n    x INTEGER\nEND STRUCT\n";
        assertCrash(
                ErrorCode.UNKNOWN_STRUCT_ENTITY, 8, point + "VARIABLE p STRUCT point\nBEGIN\n    y 1\nEND VARIABLE\n");
        assertCrash(
                ErrorCode.SYNTAX_ERROR_VARIABLE,
                9,
                point + "VARIABLE p STRUCT point\nBEGIN\n    x 1\n    x 2\nEND VARIABLE\n");
        assertCrash(
                ErrorCode.SYNTAX_ERROR_TYPE_MISMATCH,
                8,
                point + "VARIABLE p STRUCT point\nBEGIN\n    x \"1\"\nEND VARIABLE\n");
        assertCrash(
                ErrorCode.SYNTAX_ERROR_TYPE_MISMATCH, 4, "VARIABLE a ARRAY INTEGER\nBEGIN\n    1 2.5\nEND VARIABLE\n");
        assertCrash(ErrorCode.SYNTAX_ERROR_VARIABLE, 4, "VARIABLE a ARRAY INTEGER\nBEGIN\n    key 1\nEND VARIABLE\n");
        assertCrash(
                ErrorCode.SYNTAX_ERROR_VARIABLE, 5, "VARIABLE h HASH INTEGER\nBEGIN\n    a 1\n    a 2\nEND VARIABLE\n");
        assertCrash(
                ErrorCode.SYNTAX_ERROR_CONSTANT,
                4,
                "CONSTANT h HASH INTEGER\nBEGIN\n    a\n    BEGIN\n        1\n    END\nEND CONSTANT\n");
        assertCrash(ErrorCode.STRING_TOO_LONG, 4, "VARIABLE a ARRAY STRING 1\nBEGIN\n    \"ab\"\nEND VARIABLE\n");
    }

    @Test
    void stringLengthLimitsApplyToElements() {
        MapScope scope = run("VARIABLE a ARRAY STRING 2\nBEGIN\n    \"ab\" \"c\"\nEND VARIABLE\n");
        assertEquals(
                new StringValue("c"),
                ((ArrayValue) scope.resolve("a").value()).elements().get(1));
    }
}
