package de.joernott.c0de.core.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class TextScreenTest {
    private static List<String> texts(List<TextScreen.Row> rows) {
        return rows.stream().map(TextScreen.Row::text).toList();
    }

    @Test
    void textWrapsAndScrolls() {
        TextScreen screen = new TextScreen(5, 3, 0);
        screen.println("abcdefg");
        assertEquals(List.of("abcde", "fg", ""), texts(screen.rows()));
        // Ending a line in the last row scrolls up, like a terminal: the cursor is at the start of an empty row
        screen.println("h");
        assertEquals(List.of("fg", "h", ""), texts(screen.rows()), "The screen didn't scroll");
        assertEquals(2, screen.row());
        assertEquals(0, screen.column());
    }

    @Test
    void aFullRowDoesNotLeaveAnEmptyRow() {
        TextScreen screen = new TextScreen(3, 3, 0);
        screen.println("abc");
        screen.println("d");
        assertEquals(List.of("abc", "d", ""), texts(screen.rows()));
    }

    @Test
    void historyKeepsTheRowsThatScrolledOff() {
        TextScreen screen = new TextScreen(10, 2, 2);
        for (int i = 0; i < 5; i++) {
            screen.println("line " + i);
        }
        assertEquals(List.of("line 2", "line 3"), texts(screen.history()));
        assertEquals(List.of("line 4", ""), texts(screen.rows()));
        screen.clear();
        assertEquals(List.of(), screen.history());
    }

    @Test
    void locateStaysOnTheScreen() {
        TextScreen screen = new TextScreen(4, 3, 0);
        screen.locate(7, -2);
        assertEquals(2, screen.row());
        assertEquals(0, screen.column());
        screen.color(4, 1);
        screen.println("x");
        assertEquals(4, screen.history().isEmpty() ? screen.rows().get(1).foregrounds()[0] : -1);
    }
}
