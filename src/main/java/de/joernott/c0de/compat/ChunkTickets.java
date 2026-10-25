package de.joernott.c0de.compat;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;

/**
 * The chunk tickets that keep the sheets and trigger blocks of running processes loaded (task 12.1). Since 1.21.5,
 * ticket types are registered and have flags instead of a comparator; the ticket of the mod loads the chunk and
 * lets it tick, like the tickets of the older versions with distance 0.
 */
public final class ChunkTickets {
    /** The ticks until a temporary ticket expires. */
    private static final int TIMEOUT = 200;

    //? if >=1.21.5 {
    /*private static java.util.function.Supplier<TicketType> ticket;
    private static java.util.function.Supplier<TicketType> temporary;
    *///?} else {
    private static final TicketType<ChunkPos> TICKET =
            TicketType.create("c0de_process", java.util.Comparator.comparingLong(ChunkPos::toLong));
    private static final TicketType<ChunkPos> TEMPORARY =
            TicketType.create("c0de_gametest", java.util.Comparator.comparingLong(ChunkPos::toLong), TIMEOUT);
    //?}


    private ChunkTickets() {}

    /** Registers the ticket type. Called once when the mod is initialized. */
    public static void register() {
        //? if >=1.21.5 {
        /*ticket = de.joernott.c0de.platform.Platform.INSTANCE.register(
                net.minecraft.core.registries.BuiltInRegistries.TICKET_TYPE,
                "process",
                () -> new TicketType(TicketType.NO_TIMEOUT, TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION));
        temporary = de.joernott.c0de.platform.Platform.INSTANCE.register(
                net.minecraft.core.registries.BuiltInRegistries.TICKET_TYPE,
                "gametest",
                () -> new TicketType(TIMEOUT, TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION));
        *///?}
    }

    /** Keeps a chunk loaded until the ticket is removed. */
    public static void add(ServerLevel level, ChunkPos chunk) {
        //? if >=1.21.5 {
        /*level.getChunkSource().addTicketWithRadius(ticket.get(), chunk, 0);
        *///?} else {
        level.getChunkSource().addRegionTicket(TICKET, chunk, 0, chunk);
        //?}
    }

    /**
     * Keeps a chunk loaded for a while. The GameTests use it: forced chunks made the NeoForge GameTest server hang
     * when it stopped.
     */
    public static void addTemporary(ServerLevel level, ChunkPos chunk) {
        //? if >=1.21.5 {
        /*level.getChunkSource().addTicketWithRadius(temporary.get(), chunk, 0);
        *///?} else {
        level.getChunkSource().addRegionTicket(TEMPORARY, chunk, 0, chunk);
        //?}
    }

    public static void remove(ServerLevel level, ChunkPos chunk) {
        //? if >=1.21.5 {
        /*level.getChunkSource().removeTicketWithRadius(ticket.get(), chunk, 0);
        *///?} else {
        level.getChunkSource().removeRegionTicket(TICKET, chunk, 0, chunk);
        //?}
    }
}
