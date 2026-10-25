package de.joernott.c0de.sheet;

import de.joernott.c0de.block.CodeBlock;
import de.joernott.c0de.core.edit.LineMoves;
import de.joernott.c0de.core.sheet.SheetGeometry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Moves and deletes lines of a sheet for the wrench and the hammer (tasks 9.4 and 9.5). Only c0de blocks in the
 * code area of the code layer are moved, with their properties.
 */
public final class SheetEditor {
    private SheetEditor() {}

    /** A c0de block with the data of its block entity, or null. */
    private record Cell(BlockState state, CompoundTag entity) {}

    /**
     * Moves lines of a sheet.
     *
     * @param moves the new z coordinate of every line that changes its place, {@link LineMoves#DELETED} for lines
     *     that are removed
     * @return the items of the removed blocks
     */
    public static List<ItemStack> apply(ServerLevel level, int sheet, Map<Integer, Integer> moves) {
        Map<Integer, Map<Integer, Cell>> lines = new HashMap<>();
        for (int z : moves.keySet()) {
            Map<Integer, Cell> cells = new HashMap<>();
            for (int x = SheetGeometry.CODE_MIN_X; x <= SheetGeometry.CODE_MAX_X; x++) {
                BlockPos pos = LevelSheet.codePos(sheet, x, z);
                BlockState state = level.getBlockState(pos);
                if (state.getBlock() instanceof CodeBlock) {
                    BlockEntity entity = level.getBlockEntity(pos);
                    cells.put(
                            x,
                            new Cell(state, entity == null ? null : entity.saveWithoutMetadata(level.registryAccess())));
                }
            }
            lines.put(z, cells);
        }
        // All lines are taken away first, because the moved lines take each other's places
        lines.forEach((z, cells) -> cells.keySet()
                .forEach(x -> level.setBlock(LevelSheet.codePos(sheet, x, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL)));
        List<ItemStack> removed = new ArrayList<>();
        moves.forEach((z, target) -> lines.get(z).forEach((x, cell) -> {
            if (target == LineMoves.DELETED) {
                removed.add(new ItemStack(cell.state().getBlock().asItem()));
                return;
            }
            place(level, LevelSheet.codePos(sheet, x, target), cell);
        }));
        SheetMarks.move(level, sheet, moves);
        return removed;
    }

    /**
     * Copies lines in front of the line {@code target}; that line and the lines after it move south. In survival
     * mode, the blocks of the copies are taken from the inventory of the player.
     *
     * @param lines the lines to copy, in ascending order
     * @return the blocks that are missing in the inventory, empty if the lines were copied
     */
    public static Map<Item, Integer> copy(
            ServerLevel level, int sheet, SortedSet<Integer> lines, int target, Player player) {
        List<Map<Integer, Cell>> copies = new ArrayList<>();
        Map<Item, Integer> needed = new LinkedHashMap<>();
        for (int z : lines) {
            Map<Integer, Cell> cells = new HashMap<>();
            for (int x = SheetGeometry.CODE_MIN_X; x <= SheetGeometry.CODE_MAX_X; x++) {
                BlockPos pos = LevelSheet.codePos(sheet, x, z);
                BlockState state = level.getBlockState(pos);
                if (state.getBlock() instanceof CodeBlock) {
                    BlockEntity entity = level.getBlockEntity(pos);
                    cells.put(
                            x,
                            new Cell(state, entity == null ? null : entity.saveWithoutMetadata(level.registryAccess())));
                    needed.merge(state.getBlock().asItem(), 1, Integer::sum);
                }
            }
            copies.add(cells);
        }
        if (!player.isCreative()) {
            Map<Item, Integer> missing = new LinkedHashMap<>();
            needed.forEach((item, count) -> {
                int available = player.getInventory().countItem(item);
                if (available < count) {
                    missing.put(item, count - available);
                }
            });
            if (!missing.isEmpty()) {
                return missing;
            }
            needed.forEach((item, count) -> player.getInventory()
                    .clearOrCountMatchingItems(stack -> stack.is(item), count, player.inventoryMenu.getCraftSlots()));
        }
        int end = endOfContent(level, sheet, target, lines.size() + 1);
        apply(level, sheet, LineMoves.insert(target, lines.size(), end));
        for (int i = 0; i < copies.size(); i++) {
            int z = target + i;
            copies.get(i).forEach((x, cell) -> place(level, LevelSheet.codePos(sheet, x, z), cell));
        }
        return Map.of();
    }

    private static void place(ServerLevel level, BlockPos pos, Cell cell) {
        level.setBlock(pos, cell.state(), Block.UPDATE_ALL);
        if (cell.entity() != null && level.getBlockEntity(pos) != null) {
            BlockEntity entity = level.getBlockEntity(pos);
            de.joernott.c0de.compat.Compat.loadBlockEntity(entity, cell.entity(), level.registryAccess());
            entity.setChanged();
            level.sendBlockUpdated(pos, cell.state(), cell.state(), Block.UPDATE_CLIENTS);
        }
    }

    /** True if a line has no c0de blocks in its code area. */
    private static boolean isEmpty(ServerLevel level, int sheet, int z) {
        for (int x = SheetGeometry.CODE_MIN_X; x <= SheetGeometry.CODE_MAX_X; x++) {
            if (level.getBlockState(LevelSheet.codePos(sheet, x, z)).getBlock() instanceof CodeBlock) {
                return false;
            }
        }
        return true;
    }

    /**
     * The end of the lines that move south when lines are inserted at a line: everything up to a gap of
     * {@code gap} empty lines, so that moving them by fewer lines doesn't join them with the lines after the gap.
     *
     * @return the z coordinate after the last line that moves
     */
    public static int endOfContent(ServerLevel level, int sheet, int from, int gap) {
        int end = from;
        int empty = 0;
        for (int z = from; z < from + LevelSheet.MAX_LINES && empty < gap; z++) {
            if (!LevelSheet.isLoaded(level, sheet, z)) {
                break;
            }
            if (isEmpty(level, sheet, z)) {
                empty++;
            } else {
                empty = 0;
                end = z + 1;
            }
        }
        return end;
    }

    /**
     * The end of the lines that move up when lines are deleted: the first empty line after them.
     *
     * @return the z coordinate of that empty line
     */
    public static int endOfFollowingLines(ServerLevel level, int sheet, int to) {
        return to + 1 + LevelSheet.readGroup(level, sheet, to + 1).map(List::size).orElse(0);
    }
}
