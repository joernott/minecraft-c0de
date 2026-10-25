package de.joernott.c0de.dimension;

import de.joernott.c0de.C0de;
import de.joernott.c0de.compat.Compat;
import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.platform.Platform;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/** The c0de dimension: its registration and how players enter and leave it. */
public final class C0deDimension {
    /** The dimension, defined by the data files {@code data/c0de/dimension/c0de.json} and {@code dimension_type}. */
    public static final ResourceKey<Level> LEVEL = ResourceKey.create(Registries.DIMENSION, C0de.id("c0de"));

    /** Highest number of sheets that is searched for a free sheet. */
    static final int MAX_SHEET_SEARCH = 1024;

    private C0deDimension() {}

    public static void register() {
        Platform.INSTANCE.register(BuiltInRegistries.CHUNK_GENERATOR, "greenbar", () -> GreenbarChunkGenerator.CODEC);
    }

    /**
     * Teleports the player into the c0de dimension, in front of the sheet of the program of the trigger
     * block or of the first free sheet. The current position is stored if the player comes from another
     * dimension.
     */
    public static void enter(ServerPlayer player, BlockPos triggerBlock) {
        ServerLevel level = Compat.server(player).getLevel(LEVEL);
        if (level == null) {
            C0de.LOGGER.error("The c0de dimension {} doesn't exist", LEVEL.location());
            player.sendSystemMessage(Component.translatable("message.c0de.no_dimension"));
            return;
        }
        if (!player.level().dimension().equals(LEVEL)) {
            ReturnPositions.get(Compat.server(player)).store(player);
        }
        // Trigger blocks can't be configured with a program yet (sprint 8), so the player always goes to
        // the first free sheet.
        int sheet = firstFreeSheet(level);
        Compat.teleport(
                player,
                level,
                SheetGeometry.absoluteX(sheet, SheetGeometry.CODE_MIN_X) + 0.5,
                SheetGeometry.CODE_Y,
                -2 + 0.5,
                0.0F, // facing south
                30.0F);
    }

    /**
     * Teleports the player back to the position where they entered the c0de dimension, or to their
     * respawn point if there is none.
     */
    public static void leave(ServerPlayer player) {
        Optional<ReturnPositions.Position> stored = ReturnPositions.get(Compat.server(player)).take(player.getUUID());
        if (stored.isPresent()) {
            ReturnPositions.Position position = stored.get();
            ServerLevel level = Compat.server(player).getLevel(position.dimension());
            if (level != null) {
                Compat.teleport(
                        player,
                        level,
                        position.position().x,
                        position.position().y,
                        position.position().z,
                        position.yaw(),
                        position.pitch());
                return;
            }
        }
        Compat.respawn(player);
    }

    /** The first sheet without a program, i.e. without a block at the position of the PROGRAM statement. */
    static int firstFreeSheet(ServerLevel level) {
        for (int sheet = 0; sheet < MAX_SHEET_SEARCH; sheet++) {
            BlockPos program = new BlockPos(SheetGeometry.absoluteX(sheet, SheetGeometry.CODE_MIN_X), SheetGeometry.CODE_Y, 0);
            if (level.getBlockState(program).isAir()) {
                return sheet;
            }
        }
        return MAX_SHEET_SEARCH;
    }
}
