package de.joernott.c0de.core.program;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.line.Notation;
import de.joernott.c0de.core.line.Operator;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.Type;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ProgramParserTest {
    private static final String HEADER = """
            PROGRAM demo
            NOOP
            """;

    private static Program parse(String text) {
        return ProgramParser.parse(Notation.parse(text));
    }

    /** Parses a program and expects exactly one syntax error. */
    private static void assertError(ErrorCode code, int line, String text) {
        Program program = parse(text);
        List<Statement.Invalid> errors = program.errors();
        assertEquals(1, errors.size(), "errors: " + errors);
        assertEquals(code, errors.get(0).error().code(), errors.get(0).error().toString());
        assertEquals(line, errors.get(0).error().line(), errors.get(0).error().toString());
        assertTrue(errors.get(0).error().crash());
    }

    @Test
    void parsesACompleteProgram() {
        Program program = parse(HEADER + """
                    INCLUDE c0de/strings
                STRUCT point
                BEGIN
                    x INTEGER TAG(json: "px")
                    y INTEGER
                END STRUCT
                CONSTANT LIMIT INTEGER 10
                VARIABLE names ARRAY STRING
                BEGIN
                    "a" "b"
                    "c"
                END VARIABLE
                NOOP
                FUNCTION main // the entry function
                    PARAMETER count INTEGER 3
                    RESULT total INTEGER
                BEGIN
                    VARIABLE i INTEGER
                    FOR k INTEGER 1 ; count ; 1 ;
                    BEGIN
                        IF k = 2 ;
                        BEGIN
                            BREAK
                        END
                        ELSE
                        BEGIN
                            ASSIGN total total + k
                                * 2 ;
                        END IF
                    END FOR
                    WHILE i < 3 ;
                    BEGIN
                        ASSIGN i i + 1 ;
                    END WHILE
                    REPEAT
                    BEGIN
                        ASSIGN i i - 1 ;
                    END REPEAT i > 0 ;
                    EACH name names
                    BEGIN
                        NOOP
                    END EACH
                    CALL c0de/strings/MID err
                        PARAMETER text "abc" ;
                        PARAMETER start 1 ;
                        RESULT result s
                    EXIT 0 ;
                    RETURN err
                END FUNCTION main
                """);
        assertTrue(program.errors().isEmpty(), "errors: " + program.errors());
        assertEquals("demo", program.name());
        assertEquals(51, program.endLine());
        List<Statement> statements = program.statements();
        assertEquals(new Statement.Include(2, "c0de/strings"), statements.get(0));

        Statement.StructDeclaration struct = assertInstanceOf(Statement.StructDeclaration.class, statements.get(1));
        assertEquals("point", struct.name());
        assertEquals(Map.of("json", "px"), struct.elements().get(0).tags());
        assertEquals(
                new TypeSpec.Simple(Type.Simple.INTEGER),
                struct.elements().get(1).type());

        Statement.Declaration constant = assertInstanceOf(Statement.Declaration.class, statements.get(2));
        assertTrue(constant.constant());
        assertEquals(new IntegerValue(10), constant.literal());

        Statement.Declaration names = assertInstanceOf(Statement.Declaration.class, statements.get(3));
        assertEquals(new TypeSpec.Array(new TypeSpec.Str(null)), names.type());
        assertEquals(2, names.initializer().entries().size());
        assertEquals(13, names.endLine());

        Statement.FunctionDeclaration main = assertInstanceOf(Statement.FunctionDeclaration.class, statements.get(4));
        assertEquals("main", main.name());
        assertEquals(new IntegerValue(3), main.parameters().get(0).defaultValue());
        assertEquals("total", main.results().get(0).name());
        assertEquals(18, main.body().beginLine());
        assertEquals(50, main.body().endLine());

        List<Statement> body = main.body().statements();
        Statement.For loop = assertInstanceOf(Statement.For.class, body.get(1));
        assertEquals(new Expression.Literal(new IntegerValue(1)), loop.step());
        Statement.If condition =
                assertInstanceOf(Statement.If.class, loop.body().statements().get(0));
        assertEquals(26, condition.elseLine());
        assertInstanceOf(Statement.Break.class, condition.then().statements().get(0));
        Statement.Assign assign = assertInstanceOf(
                Statement.Assign.class, condition.otherwise().statements().get(0));
        assertEquals(29, assign.endLine());
        // total + (k * 2): the multiplication on the continuation line binds stronger
        Expression.Binary sum = assertInstanceOf(Expression.Binary.class, assign.value());
        assertEquals(Operator.ADD, sum.operator());

        Statement.Repeat repeat = assertInstanceOf(Statement.Repeat.class, body.get(3));
        assertInstanceOf(Expression.Binary.class, repeat.condition());
        assertInstanceOf(Statement.Each.class, body.get(4));

        Statement.Call call = assertInstanceOf(Statement.Call.class, body.get(5));
        assertEquals("c0de/strings/MID", call.function());
        assertEquals("err", call.errorLabel());
        assertEquals(2, call.parameters().size());
        assertEquals(
                new Expression.Literal(new StringValue("abc")),
                call.parameters().get(0).value());
        assertEquals("s", call.results().get(0).target().name());
        assertEquals(47, call.endLine());

        assertInstanceOf(Statement.Exit.class, body.get(6));
        assertEquals(
                "err",
                assertInstanceOf(Statement.Return.class, body.get(7)).error().name());
    }

    @Test
    void commentsAndNoopLinesAreSkipped() {
        Program program = parse(HEADER + """
                // a comment line
                VARIABLE a INTEGER 1 // initial value
                VARIABLE b INTEGER COMMENT("disabled") 2 TAG(json: "x")
                NOOP
                """);
        assertTrue(program.errors().isEmpty(), "errors: " + program.errors());
        Statement.Declaration a = (Statement.Declaration) program.statements().get(0);
        assertEquals(new IntegerValue(1), a.literal());
        Statement.Declaration b = (Statement.Declaration) program.statements().get(1);
        assertNull(b.literal(), "Blocks after a COMMENT are ignored");
        assertTrue(b.tags().isEmpty());
    }

    @Test
    void programEndsAtTheFirstEmptyLine() {
        Program program = parse(HEADER + "VARIABLE a INTEGER\n\nVARIABLE b INTEGER\n");
        assertEquals(3, program.endLine());
        assertEquals(1, program.statements().size());
    }

    @Test
    void programHeader() {
        assertError(ErrorCode.SYNTAX_ERROR_PLACEMENT, 0, "VARIABLE a INTEGER\nNOOP\n");
        assertError(ErrorCode.SYNTAX_ERROR_PLACEMENT, 1, "PROGRAM demo\nVARIABLE a INTEGER\n");
        assertError(ErrorCode.SYNTAX_ERROR_PLACEMENT, 0, "PROGRAM LABEL\nNOOP\n");
        assertError(ErrorCode.SYNTAX_ERROR_PLACEMENT, 0, "    PROGRAM demo\nNOOP\n");
    }

    @Test
    void indentation() {
        assertError(ErrorCode.SYNTAX_ERROR_INDENTATION, 2, HEADER + "    VARIABLE a INTEGER\n");
        assertError(ErrorCode.SYNTAX_ERROR_INDENTATION, 2, HEADER + "INCLUDE c0de/strings\n");
        assertError(ErrorCode.SYNTAX_ERROR_INDENTATION, 6, HEADER + """
                NOOP
                FUNCTION f
                BEGIN
                    NOOP
                        ASSIGN a 1 ;
                END FUNCTION
                """);
        assertError(ErrorCode.SYNTAX_ERROR_INDENTATION, 4, HEADER + """
                NOOP
                FUNCTION f
                    BEGIN
                    EXIT 0 ;
                END FUNCTION
                """);
    }

    @Test
    void placement() {
        assertError(ErrorCode.SYNTAX_ERROR_PLACEMENT, 3, HEADER + "VARIABLE a INTEGER\n    INCLUDE c0de/strings\n");
        assertError(ErrorCode.SYNTAX_ERROR_PLACEMENT, 2, HEADER + "ASSIGN a 1 ;\n");
        assertError(ErrorCode.SYNTAX_ERROR_PLACEMENT, 2, HEADER + "NOOP 1\n");
        assertError(ErrorCode.SYNTAX_ERROR_PLACEMENT, 2, HEADER + "NOOP // comment after NOOP\n");
        assertError(ErrorCode.SYNTAX_ERROR_PLACEMENT, 2, HEADER + "BEGIN\n");
        assertError(ErrorCode.SYNTAX_ERROR_PLACEMENT, 2, HEADER + "END\n");
        assertError(ErrorCode.SYNTAX_ERROR_PLACEMENT, 2, HEADER + "PARAMETER a INTEGER\n");
        // FUNCTION without a NOOP line before it
        assertError(ErrorCode.SYNTAX_ERROR_PLACEMENT, 3, HEADER + """
                VARIABLE a INTEGER
                FUNCTION f
                BEGIN
                    NOOP
                END FUNCTION
                """);
        String function = HEADER + "NOOP\nFUNCTION f\nBEGIN\n%s\nEND FUNCTION\n";
        assertError(ErrorCode.SYNTAX_ERROR_PLACEMENT, 5, function.formatted("    BREAK"));
        assertError(ErrorCode.SYNTAX_ERROR_PLACEMENT, 5, function.formatted("    ELSE"));
        assertError(ErrorCode.SYNTAX_ERROR_PLACEMENT, 5, function.formatted("    STRUCT p"));
        assertError(ErrorCode.SYNTAX_ERROR_PLACEMENT, 5, function.formatted("    RESULT r INTEGER"));
        // Empty code blocks
        assertError(ErrorCode.SYNTAX_ERROR_PLACEMENT, 5, HEADER + "NOOP\nFUNCTION f\nBEGIN\nEND FUNCTION\n");
        assertError(
                ErrorCode.SYNTAX_ERROR_PLACEMENT,
                6,
                HEADER + "NOOP\nFUNCTION f\nBEGIN\n    // only a comment\nEND FUNCTION\n");
        // A RESULT before a PARAMETER
        assertError(ErrorCode.SYNTAX_ERROR_PLACEMENT, 5, HEADER + """
                NOOP
                FUNCTION f
                    RESULT r INTEGER
                    PARAMETER p INTEGER
                BEGIN
                    NOOP
                END FUNCTION
                """);
    }

    @Test
    void unknownStatement() {
        assertError(ErrorCode.SYNTAX_ERROR_UNKNOWN_STATEMENT, 2, HEADER + "a INTEGER\n");
        assertError(ErrorCode.SYNTAX_ERROR_UNKNOWN_STATEMENT, 2, HEADER + "INTEGER a\n");
    }

    @Test
    void beginWithoutEnd() {
        assertError(ErrorCode.SYNTAX_ERROR_BEGIN_WITHOUT_END, 4, HEADER + "NOOP\nFUNCTION f\nBEGIN\n    EXIT 0 ;\n");
        assertError(
                ErrorCode.SYNTAX_ERROR_BEGIN_WITHOUT_END, 6, HEADER + "NOOP\nFUNCTION f\nBEGIN\n    EXIT 0 ;\nEND\n");
        assertError(ErrorCode.SYNTAX_ERROR_BEGIN_WITHOUT_END, 8, HEADER + """
                NOOP
                FUNCTION f
                BEGIN
                    WHILE TRUE ;
                    BEGIN
                        NOOP
                    END FOR
                END FUNCTION
                """);
    }

    @Test
    void statementErrors() {
        String function = HEADER + "NOOP\nFUNCTION f\nBEGIN\n%s\nEND FUNCTION\n";
        assertError(ErrorCode.SYNTAX_ERROR_ASSIGN, 5, function.formatted("    ASSIGN 1 ;"));
        assertError(ErrorCode.SYNTAX_ERROR_ASSIGN, 5, function.formatted("    ASSIGN a 1 ; 2"));
        assertError(ErrorCode.SYNTAX_ERROR_EXPRESSION, 5, function.formatted("    ASSIGN a 1"));
        assertError(ErrorCode.SYNTAX_ERROR_EXPRESSION, 5, function.formatted("    ASSIGN a 1 + ;"));
        assertError(ErrorCode.SYNTAX_ERROR_EXIT, 5, function.formatted("    EXIT"));
        assertError(ErrorCode.SYNTAX_ERROR_CALL, 5, function.formatted("    CALL"));
        assertError(
                ErrorCode.SYNTAX_ERROR_CALL,
                7,
                function.formatted("    CALL f\n        PARAMETER a 1 ;\n        PARAMETER a 2 ;"));
        assertError(
                ErrorCode.SYNTAX_ERROR_LOOP,
                5,
                function.formatted("    FOR i STRING 1 ; 2 ;\n    BEGIN\n        NOOP\n    END FOR"));
        assertError(
                ErrorCode.SYNTAX_ERROR_LOOP,
                5,
                function.formatted("    FOR i INTEGER 1 ;\n    BEGIN\n        NOOP\n    END FOR"));
        assertError(
                ErrorCode.SYNTAX_ERROR_LOOP,
                5,
                function.formatted("    REPEAT 1\n    BEGIN\n        NOOP\n    END REPEAT TRUE ;"));
        assertError(
                ErrorCode.SYNTAX_ERROR_CONDITION,
                5,
                function.formatted("    IF TRUE ; 1\n    BEGIN\n        NOOP\n    END IF"));
        assertError(
                ErrorCode.SYNTAX_ERROR_CONDITION,
                8,
                function.formatted("    IF TRUE ;\n    BEGIN\n        NOOP\n    END\n    EXIT 0 ;"));
        assertError(ErrorCode.SYNTAX_ERROR_FUNCTION, 5, function.formatted("    RETURN 1"));
    }

    @Test
    void declarationErrors() {
        assertError(ErrorCode.SYNTAX_ERROR_VARIABLE, 2, HEADER + "VARIABLE a\n");
        assertError(ErrorCode.SYNTAX_ERROR_VARIABLE, 2, HEADER + "VARIABLE IF INTEGER\n");
        assertError(ErrorCode.SYNTAX_ERROR_VARIABLE, 2, HEADER + "VARIABLE a ARRAY\n");
        assertError(ErrorCode.SYNTAX_ERROR_VARIABLE, 2, HEADER + "VARIABLE a INTEGER 1 2\n");
        assertError(ErrorCode.SYNTAX_ERROR_VARIABLE, 2, HEADER + "VARIABLE a INTEGER TAG(xml: \"a\")\n");
        assertError(ErrorCode.SYNTAX_ERROR_VARIABLE, 2, HEADER + "VARIABLE a INTEGER TAG(json: \"a..b\")\n");
        assertError(
                ErrorCode.SYNTAX_ERROR_VARIABLE, 2, HEADER + "VARIABLE a INTEGER TAG(json: \"a\") TAG(json: \"b\")\n");
        assertError(ErrorCode.SYNTAX_ERROR_VARIABLE, 2, HEADER + "VARIABLE a ARRAY INTEGER 1\n");
        assertError(ErrorCode.SYNTAX_ERROR_TYPE_MISMATCH, 2, HEADER + "VARIABLE a INTEGER LITERAL\n");
        assertError(ErrorCode.SYNTAX_ERROR_CONSTANT, 2, HEADER + "CONSTANT a INTEGER\n");
        assertError(ErrorCode.SYNTAX_ERROR_CONSTANT, 2, HEADER + "CONSTANT a ARRAY INTEGER\n");
        assertError(ErrorCode.SYNTAX_ERROR_VARIABLE, 2, HEADER + "VARIABLE a " + "ARRAY ".repeat(9) + "INTEGER\n");
        assertError(
                ErrorCode.SYNTAX_ERROR_BEGIN_WITHOUT_END,
                5,
                HEADER + "VARIABLE a ARRAY INTEGER\nBEGIN\n    1\nEND CONSTANT\n");
    }

    @Test
    void structErrors() {
        assertError(ErrorCode.SYNTAX_ERROR_STRUCT, 2, HEADER + "STRUCT p INTEGER\nBEGIN\n    x INTEGER\nEND STRUCT\n");
        assertError(ErrorCode.SYNTAX_ERROR_STRUCT, 2, HEADER + "STRUCT p\n    x INTEGER\n");
        // Errors in element lines and the END line are reported in these lines
        assertError(
                ErrorCode.SYNTAX_ERROR_STRUCT, 5, HEADER + "STRUCT p\nBEGIN\n    x INTEGER\n    x FLOAT\nEND STRUCT\n");
        assertError(
                ErrorCode.SYNTAX_ERROR_STRUCT,
                4,
                HEADER + "STRUCT p\nBEGIN\n    x INTEGER TAG(json: \"a.b\")\nEND STRUCT\n");
        assertError(ErrorCode.SYNTAX_ERROR_STRUCT, 5, HEADER + "STRUCT p\nBEGIN\n    x INTEGER\nEND\n");
        assertError(ErrorCode.SYNTAX_ERROR_STRUCT, 4, HEADER + "STRUCT p\nBEGIN\n    x STRUCT\nEND STRUCT\n");
    }

    @Test
    void parsingContinuesAfterAnError() {
        Program program = parse(HEADER + """
                VARIABLE a
                NOOP
                FUNCTION f
                BEGIN
                    ASSIGN x 1 ;
                    ASSIGN ;
                    EXIT 0 ;
                END FUNCTION
                """);
        assertEquals(2, program.errors().size(), "errors: " + program.errors());
        Statement.FunctionDeclaration f = assertInstanceOf(
                Statement.FunctionDeclaration.class, program.statements().get(1));
        assertEquals(3, f.body().statements().size());
        assertInstanceOf(Statement.Exit.class, f.body().statements().get(2));
    }

    @Test
    void initializers() {
        Program program = parse(HEADER + """
                VARIABLE m HASH ARRAY INTEGER
                BEGIN
                    first
                    BEGIN
                        1 2
                    END
                    second
                    BEGIN
                        BEGIN
                            3
                        END
                    END
                END VARIABLE
                """);
        assertTrue(program.errors().isEmpty(), "errors: " + program.errors());
        Statement.Declaration m = (Statement.Declaration) program.statements().get(0);
        Initializer.KeyedNested first = assertInstanceOf(
                Initializer.KeyedNested.class, m.initializer().entries().get(0));
        assertEquals("first", first.key());
        assertEquals(
                List.of(new IntegerValue(1), new IntegerValue(2)),
                assertInstanceOf(
                                Initializer.Values.class,
                                first.content().entries().get(0))
                        .values());
        Initializer.KeyedNested second = assertInstanceOf(
                Initializer.KeyedNested.class, m.initializer().entries().get(1));
        assertInstanceOf(Initializer.Nested.class, second.content().entries().get(0));
    }
}
