package de.joernott.c0de.io;

import de.joernott.c0de.core.io.TextScreen;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The display walls (docs/concept/19-input-output.md): display blocks that touch each other in the plane of
 * their front, all facing the same direction, form a wall with one screen. The block at the top left of the
 * wall (the origin) holds the screen; all blocks share the binding. Walls are found anew whenever a display block
 * is placed, removed or turned.
 */
public final class DisplayWalls {
    /** The most blocks of a wall, so that searching a wall stays fast. */
    private static final int MAX_BLOCKS = 256;

    private DisplayWalls() {}

    /** The direction to the right of a viewer in front of the block; the text runs in this direction. */
    public static Direction right(Direction facing) {
        return switch (facing) {
            case NORTH -> Direction.WEST;
            case SOUTH -> Direction.EAST;
            case EAST -> Direction.NORTH;
            case WEST -> Direction.SOUTH;
            case UP, DOWN -> Direction.EAST;
        };
    }

    /** The direction to the top of the text: up for upright walls, north (south from below) for flat ones. */
    public static Direction up(Direction facing) {
        return switch (facing) {
            case UP -> Direction.NORTH;
            case DOWN -> Direction.SOUTH;
            default -> Direction.UP;
        };
    }

    private static int along(BlockPos pos, Direction direction) {
        return pos.getX() * direction.getStepX() + pos.getY() * direction.getStepY() + pos.getZ() * direction.getStepZ();
    }

    /** The display blocks of the wall of a block, or an empty list if it isn't a display block. */
    public static List<BlockPos> wall(ServerLevel level, BlockPos start) {
        BlockState first = level.getBlockState(start);
        if (!(first.getBlock() instanceof DisplayBlock)) {
            return List.of();
        }
        Direction facing = first.getValue(DisplayBlock.FACING);
        Direction[] neighbors = {right(facing), right(facing).getOpposite(), up(facing), up(facing).getOpposite()};
        List<BlockPos> blocks = new ArrayList<>();
        Set<BlockPos> seen = new HashSet<>(List.of(start));
        Deque<BlockPos> open = new ArrayDeque<>(List.of(start));
        while (!open.isEmpty() && blocks.size() < MAX_BLOCKS) {
            BlockPos pos = open.poll();
            blocks.add(pos);
            for (Direction direction : neighbors) {
                BlockPos next = pos.relative(direction);
                if (seen.add(next)
                        && level.isLoaded(next)
                        && level.getBlockState(next).getBlock() instanceof DisplayBlock
                        && level.getBlockState(next).getValue(DisplayBlock.FACING) == facing) {
                    open.add(next);
                }
            }
        }
        return blocks;
    }

    /**
     * Finds the walls around a changed position again; the position may still be a display block or not.
     *
     * @param removedOrigin the position of a removed origin, whose place in the device order the wall keeps
     * @param removedScreen the screen of the removed origin, which the wall keeps, or null
     */
    public static void changed(ServerLevel level, BlockPos pos, BlockPos removedOrigin, CompoundTag removedScreen) {
        Set<BlockPos> done = new HashSet<>();
        List<BlockPos> starts = new ArrayList<>(List.of(pos));
        for (Direction direction : Direction.values()) {
            starts.add(pos.relative(direction));
        }
        for (BlockPos start : starts) {
            if (!done.contains(start) && level.getBlockState(start).getBlock() instanceof DisplayBlock) {
                List<BlockPos> wall = wall(level, start);
                done.addAll(wall);
                rebuild(level, wall, removedOrigin, removedScreen);
            }
        }
    }

    /**
     * Makes the block at the top left the origin of a wall: it gets the screen of the old origin (resized to the
     * wall) and all blocks get the binding of the wall. The device registry follows the origin.
     */
    private static void rebuild(
            ServerLevel level, List<BlockPos> wall, BlockPos removedOrigin, CompoundTag removedScreen) {
        Direction facing = level.getBlockState(wall.get(0)).getValue(DisplayBlock.FACING);
        Direction right = right(facing);
        Direction up = up(facing);
        int minRight = Integer.MAX_VALUE;
        int maxRight = Integer.MIN_VALUE;
        int minUp = Integer.MAX_VALUE;
        int maxUp = Integer.MIN_VALUE;
        BlockPos origin = null;
        for (BlockPos pos : wall) {
            int r = along(pos, right);
            int u = along(pos, up);
            minRight = Math.min(minRight, r);
            maxRight = Math.max(maxRight, r);
            minUp = Math.min(minUp, u);
            maxUp = Math.max(maxUp, u);
            if (origin == null
                    || u > along(origin, up)
                    || (u == along(origin, up) && r < along(origin, right))) {
                origin = pos;
            }
        }
        // The screen and the binding of the wall: those of its old origin, otherwise of any bound block
        CompoundTag screen = removedScreen;
        BlockPos oldOrigin = removedOrigin;
        String program = "";
        String function = "";
        for (BlockPos pos : wall) {
            if (level.getBlockEntity(pos) instanceof DisplayBlockEntity entity) {
                if (entity.isOrigin() && entity.screen() != null && (screen == null || screen == removedScreen)) {
                    screen = ScreenNbt.write(entity.screen());
                    oldOrigin = pos;
                }
                if (program.isEmpty() && !entity.program().isEmpty()) {
                    program = entity.program();
                    function = entity.function();
                }
            }
        }
        int width = maxRight - minRight + 1;
        int height = maxUp - minUp + 1;
        TextScreen newScreen = screen == null
                ? new TextScreen(width * DisplayBlockEntity.COLUMNS, height * DisplayBlockEntity.ROWS, 0)
                : ScreenNbt.read(screen, width * DisplayBlockEntity.COLUMNS, height * DisplayBlockEntity.ROWS, 0);
        for (BlockPos pos : wall) {
            if (level.getBlockEntity(pos) instanceof DisplayBlockEntity entity) {
                if (!entity.program().equals(program) || !entity.function().equals(function)) {
                    entity.setBinding(program, function);
                }
                if (pos.equals(origin)) {
                    entity.makeOrigin(
                            width, height, along(origin, right) - minRight, maxUp - along(origin, up), newScreen);
                } else {
                    entity.makePart();
                }
            }
        }
        IoDevices devices = IoDevices.get(level.getServer());
        if (program.isEmpty()) {
            if (oldOrigin != null) {
                devices.remove(level.dimension(), oldOrigin);
            }
            return;
        }
        BlockPos wallOrigin = origin;
        boolean registered = devices.devices(program, function).stream()
                .anyMatch(device -> device.level().equals(level.dimension()) && device.pos().equals(wallOrigin));
        if (oldOrigin != null && !oldOrigin.equals(origin)) {
            devices.move(level.dimension(), oldOrigin, origin);
            registered = true;
        }
        if (!registered) {
            devices.bind(IoDevices.Kind.DISPLAY, level.dimension(), origin, program, function);
        }
    }

    /** Binds all blocks of the wall of a block; the wall moves to the end of the order of the devices. */
    public static void bind(ServerLevel level, BlockPos pos, String program, String function) {
        List<BlockPos> wall = wall(level, pos);
        BlockPos origin = null;
        for (BlockPos member : wall) {
            if (level.getBlockEntity(member) instanceof DisplayBlockEntity entity) {
                entity.setBinding(program, function);
                if (entity.isOrigin()) {
                    origin = member;
                    // A new binding starts with an empty screen
                    entity.screen().clear();
                    entity.screenChanged();
                }
            }
        }
        if (origin == null) {
            return;
        }
        IoDevices devices = IoDevices.get(level.getServer());
        if (program.isEmpty()) {
            devices.remove(level.dimension(), origin);
        } else {
            devices.bind(IoDevices.Kind.DISPLAY, level.dimension(), origin, program, function);
        }
    }
}
