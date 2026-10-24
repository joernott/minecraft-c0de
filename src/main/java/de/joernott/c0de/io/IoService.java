package de.joernott.c0de.io;

import de.joernott.c0de.core.io.Console;
import net.minecraft.server.MinecraftServer;

/**
 * The devices of the processes (docs/concept/19-input-output.md): the display walls, I/O blocks, laptops and
 * tablets bound to a program and function. A process works with them through the {@link Console} of its program
 * and entry function.
 */
public final class IoService {
    private IoService() {}

    /** The console of the devices bound to a program and function. */
    public static Console console(MinecraftServer server, String program, String function) {
        return new DeviceConsole(server, program, function);
    }

    /**
     * The devices bound to a program and function. Devices are added by the tasks 11.6 to 11.9; until then, a
     * process has no devices: PRINT writes nothing, and WRITE and INPUT wait.
     */
    private record DeviceConsole(MinecraftServer server, String program, String function) implements Console {}
}
