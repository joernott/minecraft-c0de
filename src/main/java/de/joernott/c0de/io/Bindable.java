package de.joernott.c0de.io;

import net.minecraft.server.level.ServerLevel;

/**
 * A device that is bound to a program and function, like a trigger block (docs/concept/19-input-output.md):
 * display blocks, I/O blocks and laptops. Its dialog lists the programs and their functions.
 */
public interface Bindable {
    /** The program, or the empty string if the device is not bound. */
    String program();

    /** The function, or the empty string if the device is not bound. */
    String function();

    /** Binds the device; an empty program unbinds it. */
    void bind(ServerLevel level, String program, String function);
}
