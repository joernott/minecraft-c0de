package de.joernott.c0de.library;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * What the functions of c0de/minecraft need to know about the process that calls them: the server, the trigger
 * block that started it, its owner, the program and the PID. It is the host of the cursor settings.
 *
 * @param owner the player who configured the trigger block, or null
 */
public record ProcessHost(
        MinecraftServer server,
        ResourceKey<Level> triggerLevel,
        BlockPos triggerPos,
        UUID owner,
        String program,
        int pid) {
    /** The level of the trigger block, or null if it doesn't exist. */
    public ServerLevel level() {
        return server.getLevel(triggerLevel);
    }
}
