package de.joernott.c0de.library;

import de.joernott.c0de.core.io.Console;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0de.io.IoService;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * What the functions of c0de/minecraft need to know about the process that calls them: the server, the trigger
 * block that started it, its owner, the program, the entry function and the PID. It is the host of the cursor
 * settings, and the {@link Console} of the process: the devices bound to its program and entry function.
 *
 * @param owner the player who configured the trigger block, or null
 */
public record ProcessHost(
        MinecraftServer server,
        ResourceKey<Level> triggerLevel,
        BlockPos triggerPos,
        UUID owner,
        String program,
        String function,
        int pid)
        implements Console {
    /** The level of the trigger block, or null if it doesn't exist. */
    public ServerLevel level() {
        return server.getLevel(triggerLevel);
    }

    private Console devices() {
        return IoService.console(server, program, function);
    }

    @Override
    public void print(String text) {
        devices().print(text);
    }

    @Override
    public boolean write(String name, Value value) {
        return devices().write(name, value);
    }

    @Override
    public Optional<Value> input(String name, Type type) {
        return devices().input(name, type);
    }

    @Override
    public void cls() {
        devices().cls();
    }

    @Override
    public void locate(int row, int column) {
        devices().locate(row, column);
    }

    @Override
    public int row() {
        return devices().row();
    }

    @Override
    public int column() {
        return devices().column();
    }

    @Override
    public void color(int foreground, int background) {
        devices().color(foreground, background);
    }

    @Override
    public String inkey() {
        return devices().inkey();
    }
}
