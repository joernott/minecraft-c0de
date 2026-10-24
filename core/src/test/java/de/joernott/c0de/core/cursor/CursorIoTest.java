package de.joernott.c0de.core.cursor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0de.core.io.Console;
import de.joernott.c0de.core.io.TextScreen;
import de.joernott.c0de.core.library.standard.StandardLibraries;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** The built-in functions PRINT, WRITE, INPUT and the screen functions (docs/concept/19-input-output.md). */
class CursorIoTest {
    /** A console with one screen, values to read and room for a number of writes. */
    private static final class TestConsole implements Console {
        final TextScreen screen = new TextScreen(20, 4, 0);
        final Deque<Value> inputs = new ArrayDeque<>();
        final List<String> requested = new ArrayList<>();
        final List<String> written = new ArrayList<>();
        final Deque<String> keys = new ArrayDeque<>();
        int writable;

        @Override
        public void print(String text) {
            screen.println(text);
        }

        @Override
        public boolean write(String name, Value value) {
            if (writable == 0) {
                return false;
            }
            writable--;
            written.add(name + "=" + value.text());
            return true;
        }

        @Override
        public Optional<Value> input(String name, Type type) {
            requested.add(name + ":" + type);
            return Optional.ofNullable(inputs.poll());
        }

        @Override
        public void cls() {
            screen.clear();
        }

        @Override
        public void locate(int row, int column) {
            screen.locate(row, column);
        }

        @Override
        public int row() {
            return screen.row();
        }

        @Override
        public int column() {
            return screen.column();
        }

        @Override
        public void color(int foreground, int background) {
            screen.color(foreground, background);
        }

        @Override
        public String inkey() {
            return keys.isEmpty() ? "" : keys.poll();
        }
    }

    private static Cursor cursor(String program, TestConsole console) {
        CursorSettings settings = new CursorSettings(
                CursorSettings.DEFAULT_STACK_SIZE,
                StandardLibraries.registry(),
                CursorTestSupport.CLOCK,
                java.time.ZoneOffset.UTC,
                console,
                42);
        return new Cursor(CursorTestSupport.program(program), "main", Map.of(), settings);
    }

    @Test
    void printWritesLinesOnTheScreen() {
        TestConsole console = new TestConsole();
        Cursor cursor = cursor("""
                FUNCTION main
                BEGIN
                    VARIABLE count INTEGER 41
                    CALL PRINT
                        PARAMETER value "Hello" ;
                    CALL PRINT
                        PARAMETER value count + 1 ;
                END FUNCTION
                """, console);
        cursor.run(100);
        assertEquals(CursorState.FINISHED, cursor.state(), () -> "crashed: " + cursor.error());
        assertEquals("Hello", console.screen.rows().get(0).text());
        assertEquals("42", console.screen.rows().get(1).text());
    }

    @Test
    void inputWaitsForAValue() {
        TestConsole console = new TestConsole();
        Cursor cursor = cursor("""
                FUNCTION main
                    RESULT doubled INTEGER
                BEGIN
                    VARIABLE count INTEGER
                    CALL INPUT
                        PARAMETER variable count ;
                    ASSIGN doubled count * 2 ;
                END FUNCTION
                """, console);
        cursor.run(20);
        assertEquals(CursorState.WAITING, cursor.state(), "The cursor didn't wait for the input");
        assertTrue(console.requested.contains("count:INTEGER"), "Requested: " + console.requested);
        console.inputs.add(new IntegerValue(21));
        cursor.run(20);
        assertEquals(CursorState.FINISHED, cursor.state(), () -> "crashed: " + cursor.error());
        assertEquals(new IntegerValue(42), cursor.results().get("doubled"));
    }

    @Test
    void inputNeedsAWholeVariable() {
        TestConsole console = new TestConsole();
        console.inputs.add(new StringValue("b"));
        Cursor cursor = cursor("""
                FUNCTION main
                BEGIN
                    VARIABLE names ARRAY STRING
                    ASSIGN names[0] "a" ;
                    CALL INPUT
                        PARAMETER variable names[0] ;
                END FUNCTION
                """, console);
        cursor.run(100);
        assertEquals(CursorState.CRASHED, cursor.state());
        assertEquals(List.of(), console.requested);
    }

    @Test
    void writeWaitsForAnItemAndUsesTheNameOfTheLabel() {
        TestConsole console = new TestConsole();
        Cursor cursor = cursor("""
                FUNCTION main
                BEGIN
                    VARIABLE count INTEGER 7
                    CALL WRITE
                        PARAMETER value count ;
                    CALL WRITE
                        PARAMETER value count + 1 ;
                    CALL WRITE
                        PARAMETER value count ;
                        PARAMETER name "total" ;
                END FUNCTION
                """, console);
        cursor.run(20);
        assertEquals(CursorState.WAITING, cursor.state(), "The cursor didn't wait for an item");
        console.writable = 3;
        cursor.run(20);
        assertEquals(CursorState.FINISHED, cursor.state(), () -> "crashed: " + cursor.error());
        assertEquals(List.of("count=7", "output=8", "total=7"), console.written);
    }

    @Test
    void screenFunctions() {
        TestConsole console = new TestConsole();
        console.keys.add("q");
        Cursor cursor = cursor("""
                FUNCTION main
                    RESULT row INTEGER
                    RESULT column INTEGER
                    RESULT key STRING
                BEGIN
                    CALL PRINT
                        PARAMETER value "dirty" ;
                    CALL CLS
                    CALL LOCATE
                        PARAMETER row 2 ;
                        PARAMETER column 3 ;
                    CALL COLOR
                        PARAMETER foreground 12 ;
                    CALL CSRLIN
                        RESULT result row
                    CALL POS
                        RESULT result column
                    CALL INKEY
                        RESULT result key
                    CALL PRINT
                        PARAMETER value "x" ;
                END FUNCTION
                """, console);
        cursor.run(200);
        assertEquals(CursorState.FINISHED, cursor.state(), () -> "crashed: " + cursor.error());
        assertEquals(new IntegerValue(2), cursor.results().get("row"));
        assertEquals(new IntegerValue(3), cursor.results().get("column"));
        assertEquals(new StringValue("q"), cursor.results().get("key"));
        assertEquals("", console.screen.rows().get(0).text(), "CLS didn't clear the screen");
        assertEquals("   x", console.screen.rows().get(2).text());
        assertEquals(12, console.screen.rows().get(2).foregrounds()[3]);
    }

    @Test
    void colorsOutsideTheRangeAreInvalid() {
        Cursor cursor = cursor("""
                FUNCTION main
                BEGIN
                    CALL COLOR
                        PARAMETER foreground 16 ;
                END FUNCTION
                """, new TestConsole());
        cursor.run(100);
        assertEquals(CursorState.CRASHED, cursor.state());
        assertEquals(new IntegerValue(309), ((de.joernott.c0de.core.value.StructValue) cursor.error()).get("code"));
    }
}
