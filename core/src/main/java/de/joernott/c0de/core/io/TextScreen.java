package de.joernott.c0de.core.io;

import java.util.ArrayList;
import java.util.List;

/**
 * The screen of a display wall, laptop or tablet (docs/concept/19-input-output.md): a grid of characters, each
 * with a foreground and a background color (0 to 15, background 0 is the color of the screen), and a cursor.
 * Text continues in the next row at the end of a row, and the screen scrolls up below the last row. The rows that
 * scroll off the top are kept in a history of a limited size (terminals); displays keep none.
 */
public final class TextScreen {
    /** The default foreground color: white. */
    public static final int DEFAULT_FOREGROUND = 15;

    private final int width;
    private final int height;
    private final int historySize;
    private final List<Row> history = new ArrayList<>();
    private Row[] rows;
    private int row;
    private int column;
    private int foreground = DEFAULT_FOREGROUND;
    private int background;

    /** A row of the screen: the characters (code points) and their colors. */
    public record Row(int[] characters, byte[] foregrounds, byte[] backgrounds) {
        static Row empty(int width) {
            int[] characters = new int[width];
            java.util.Arrays.fill(characters, ' ');
            byte[] foregrounds = new byte[width];
            java.util.Arrays.fill(foregrounds, (byte) DEFAULT_FOREGROUND);
            return new Row(characters, foregrounds, new byte[width]);
        }

        /** The characters as text, without trailing spaces. */
        public String text() {
            return new String(characters, 0, characters.length).stripTrailing();
        }
    }

    public TextScreen(int width, int height, int historySize) {
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        this.historySize = Math.max(0, historySize);
        clear();
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int row() {
        return row;
    }

    public int column() {
        return column;
    }

    public int foreground() {
        return foreground;
    }

    public int background() {
        return background;
    }

    /** The rows of the screen, from the top. */
    public List<Row> rows() {
        return List.of(rows);
    }

    /** The rows that scrolled off the top, the oldest first. */
    public List<Row> history() {
        return List.copyOf(history);
    }

    /** Writes the text at the cursor and ends the line (PRINT). */
    public void println(String text) {
        text.codePoints().forEach(this::put);
        newLine();
    }

    /** Writes one character at the cursor; a line break ends the line. */
    public void put(int character) {
        if (character == '\n') {
            newLine();
            return;
        }
        if (column >= width) {
            newLine();
        }
        Row current = rows[row];
        current.characters()[column] = character < ' ' ? ' ' : character;
        current.foregrounds()[column] = (byte) foreground;
        current.backgrounds()[column] = (byte) background;
        column++;
    }

    private void newLine() {
        column = 0;
        if (row < height - 1) {
            row++;
        } else {
            scroll();
        }
    }

    private void scroll() {
        if (historySize > 0) {
            history.add(rows[0]);
            if (history.size() > historySize) {
                history.remove(0);
            }
        }
        System.arraycopy(rows, 1, rows, 0, height - 1);
        rows[height - 1] = Row.empty(width);
    }

    /** Clears the screen and its history; the cursor moves to the top left (CLS). */
    public void clear() {
        rows = new Row[height];
        for (int i = 0; i < height; i++) {
            rows[i] = Row.empty(width);
        }
        history.clear();
        row = 0;
        column = 0;
    }

    /** Moves the cursor; positions outside of the screen are moved to its nearest edge (LOCATE). */
    public void locate(int newRow, int newColumn) {
        row = Math.max(0, Math.min(height - 1, newRow));
        column = Math.max(0, Math.min(width - 1, newColumn));
    }

    /** The colors of the characters written from now on (COLOR). */
    public void color(int newForeground, int newBackground) {
        foreground = newForeground;
        background = newBackground;
    }

    /** Restores a screen, e.g. from a saved world. Rows that don't fit are cut off. */
    public void restore(List<Row> savedRows, List<Row> savedHistory, int savedRow, int savedColumn, int fg, int bg) {
        clear();
        for (int i = 0; i < Math.min(height, savedRows.size()); i++) {
            rows[i] = fit(savedRows.get(i));
        }
        savedHistory.stream()
                .skip(Math.max(0, savedHistory.size() - historySize))
                .forEach(saved -> history.add(fit(saved)));
        locate(savedRow, savedColumn);
        column = Math.max(0, Math.min(width, savedColumn));
        color(fg, bg);
    }

    private Row fit(Row saved) {
        Row result = Row.empty(width);
        int length = Math.min(width, saved.characters().length);
        System.arraycopy(saved.characters(), 0, result.characters(), 0, length);
        System.arraycopy(saved.foregrounds(), 0, result.foregrounds(), 0, length);
        System.arraycopy(saved.backgrounds(), 0, result.backgrounds(), 0, length);
        return result;
    }
}
