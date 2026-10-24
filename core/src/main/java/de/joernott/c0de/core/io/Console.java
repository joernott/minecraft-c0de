package de.joernott.c0de.core.io;

import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import java.util.Optional;

/**
 * The devices a process works with (docs/concept/19-input-output.md): the screens for PRINT and the screen
 * functions, the I/O blocks for WRITE and INPUT, and the terminals of laptops and tablets for INPUT and INKEY.
 * The mod implements it for the devices bound to the program and function of a process; without devices,
 * {@link #NONE} prints nothing and makes WRITE and INPUT wait.
 */
public interface Console {
    /** No devices: output is discarded, WRITE and INPUT wait forever. */
    Console NONE = new Console() {};

    /** PRINT: writes the text and ends the line on all screens. */
    default void print(String text) {}

    /**
     * WRITE: writes a value into the I/O blocks.
     *
     * @param name the name of the variable item
     * @return false if no I/O block can take the value yet, so the cursor waits
     */
    default boolean write(String name, Value value) {
        return false;
    }

    /**
     * INPUT: a value for a variable from an I/O block or a terminal.
     *
     * @param name the name of the variable, without index
     * @param type the type the value must have
     * @return the value, or empty if there is none yet, so the cursor waits
     */
    default Optional<Value> input(String name, Type type) {
        return Optional.empty();
    }

    /** CLS: clears the screens. */
    default void cls() {}

    /** LOCATE: moves the cursor of the screens. */
    default void locate(int row, int column) {}

    /** CSRLIN: the row of the cursor of the first screen, 0 without screens. */
    default int row() {
        return 0;
    }

    /** POS: the column of the cursor of the first screen, 0 without screens. */
    default int column() {
        return 0;
    }

    /** COLOR: the colors of the text printed from now on, 0 to 15. */
    default void color(int foreground, int background) {}

    /** INKEY: the oldest key typed on a terminal while no INPUT waited, or the empty string. */
    default String inkey() {
        return "";
    }
}
