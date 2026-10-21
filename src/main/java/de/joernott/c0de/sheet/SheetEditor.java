package de.joernott.c0de.sheet;

import de.joernott.c0de.block.CodeBlock;
import de.joernott.c0de.core.edit.LineMoves;
import de.joernott.c0de.core.sheet.SheetGeometry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
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
            BlockPos pos = LevelSheet.codePos(sheet, x, target);
            level.setBlock(pos, cell.state(), Block.UPDATE_ALL);
            if (cell.entity() != null && level.getBlockEntity(pos) != null) {
                BlockEntity entity = level.getBlockEntity(pos);
                entity.loadWithComponents(cell.entity(), level.registryAccess());
                entity.setChanged();
                level.sendBlockUpdated(pos, cell.state(), cell.state(), Block.UPDATE_CLIENTS);
            }
        }));
        SheetMarks.move(level, sheet, moves);
        return removed;
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
