package de.joernott.c0de.process;

import de.joernott.c0de.platform.Platform;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Who starts a trigger block (task 11.11.8, docs/concept/13-running-programs.md): the entity that pressed a button,
 * switched a lever or stands on a pressure plate touching the trigger block. Other redstone signals have no
 * activator.
 *
 * @param kind what kind of entity started the trigger
 * @param player the player who started it, or null
 */
public record Activator(Kind kind, UUID player) {
    /** Nobody: a redstone signal from a clock, a comparator, a lever switched long ago, ... */
    public static final Activator NONE = new Activator(Kind.NONE, null);

    /** The kinds of activators. */
    public enum Kind {
        NONE,
        MOB,
        PLAYER
    }

    /** How many ticks after a player used a button or lever its signal is still theirs. */
    private static final long USE_TICKS = 5;

    private record Use(UUID player, long tick) {}

    /** The players who used buttons and levers recently, by dimension and position. */
    private static final Map<ResourceKey<Level>, Map<BlockPos, Use>> USES = new HashMap<>();

    /** Remembers the players who use buttons and levers. Called once when the mod is initialized. */
    public static void register() {
        Platform.INSTANCE.onUseBlock((player, level, pos) -> {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof ButtonBlock || state.getBlock() instanceof LeverBlock) {
                Map<BlockPos, Use> uses = USES.computeIfAbsent(level.dimension(), key -> new HashMap<>());
                long tick = level.getGameTime();
                uses.values().removeIf(use -> tick - use.tick() > USE_TICKS);
                uses.put(pos.immutable(), new Use(player.getUUID(), tick));
            }
        });
    }

    /** A player started it. */
    public static Activator player(Player player) {
        return new Activator(Kind.PLAYER, player.getUUID());
    }

    /** The activator of the signal a trigger block just received: players are preferred to mobs. */
    public static Activator find(ServerLevel level, BlockPos trigger) {
        Activator found = NONE;
        for (Direction direction : Direction.values()) {
            BlockPos pos = trigger.relative(direction);
            Activator activator = at(level, pos, level.getBlockState(pos));
            if (activator.kind() == Kind.PLAYER) {
                return activator;
            }
            if (activator.kind() == Kind.MOB) {
                found = activator;
            }
        }
        return found;
    }

    private static Activator at(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof ButtonBlock || state.getBlock() instanceof LeverBlock) {
            Use use = USES.getOrDefault(level.dimension(), Map.of()).get(pos);
            if (use != null && level.getGameTime() - use.tick() <= USE_TICKS) {
                return new Activator(Kind.PLAYER, use.player());
            }
            // Arrows and other projectiles press wooden buttons for whoever shot them
            for (Projectile projectile : level.getEntitiesOfClass(Projectile.class, new AABB(pos))) {
                Activator shooter = of(projectile.getOwner());
                if (shooter.kind() != Kind.NONE) {
                    return shooter;
                }
            }
            return NONE;
        }
        if (state.getBlock() instanceof BasePressurePlateBlock) {
            Activator found = NONE;
            for (Entity entity : level.getEntities((Entity) null, new AABB(pos))) {
                Activator activator = of(entity);
                if (activator.kind() == Kind.PLAYER) {
                    return activator;
                }
                if (activator.kind() == Kind.MOB) {
                    found = activator;
                }
            }
            return found;
        }
        return NONE;
    }

    private static Activator of(Entity entity) {
        if (entity instanceof Player player && !player.isSpectator()) {
            return player(player);
        }
        if (entity instanceof LivingEntity) {
            return new Activator(Kind.MOB, null);
        }
        return NONE;
    }
}
