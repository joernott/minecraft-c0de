package de.joernott.c0de.core.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0de.core.value.IntegerValue;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ErrorCodeTest {
    @Test
    void codesAreUniqueAndBelowTheUserRange() {
        Set<Integer> codes = new HashSet<>();
        for (ErrorCode error : ErrorCode.values()) {
            assertTrue(codes.add(error.code()), "duplicate " + error.code());
            assertTrue(error.code() < ErrorCode.FIRST_USER_CODE);
            assertEquals(error, ErrorCode.of(error.code()));
        }
        assertNull(ErrorCode.of(1000));
    }

    @Test
    void handlingMatchesTheConcept() {
        assertEquals(ErrorCode.Handling.CRASH, ErrorCode.SYNTAX_ERROR_INDENTATION.handling());
        assertEquals(ErrorCode.Handling.CRASH, ErrorCode.STACK_OVERFLOW.handling());
        assertEquals(ErrorCode.Handling.RAISE, ErrorCode.DIVISION_BY_ZERO.handling());
        assertEquals(ErrorCode.Handling.RAISE, ErrorCode.UNKNOWN_STRUCT_ENTITY.handling());
        assertEquals(303, ErrorCode.DIVISION_BY_ZERO.code());
        assertEquals(311, ErrorCode.PERMISSION_DENIED.code());
        assertEquals(ErrorCode.Handling.CRASH, ErrorCode.PROCESS_ABORTED.handling());
        assertEquals(206, ErrorCode.PROCESS_ABORTED.code());
    }

    @Test
    void builtinConstants() {
        Map<String, ?> constants = ErrorCode.builtinConstants();
        assertEquals(ErrorCode.values().length, constants.size());
        assertEquals(new IntegerValue(303), constants.get("DIVISION_BY_ZERO"));
        assertEquals(new IntegerValue(0), constants.get("OK"));
    }

    @Test
    void exceptions() {
        C0deException raised = C0deException.of(ErrorCode.TYPE_MISMATCH, "x");
        assertFalse(raised.crash());
        assertEquals(C0deException.NO_LINE, raised.line());
        C0deException located = raised.inLine(7);
        assertEquals(7, located.line());
        assertEquals(7, located.inLine(9).line(), "A located error keeps its line");
        assertTrue(located.asCrash().crash());
        assertTrue(C0deException.crash(ErrorCode.STRING_TOO_LONG, "x").crash());
        assertTrue(C0deException.at(3, ErrorCode.SYNTAX_ERROR_PLACEMENT, "x").crash());
    }
}
