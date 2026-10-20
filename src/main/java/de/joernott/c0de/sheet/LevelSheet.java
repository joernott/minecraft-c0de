package de.joernott.c0de.sheet;

import de.joernott.c0de.block.CodeBlock;
import de.joernott.c0de.blockentity.PropertiesBlockEntity;
import de.joernott.c0de.core.edit.SheetView;
import de.joernott.c0de.core.line.Block;
import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.dimension.C0deDimension;
import java.time.ZoneId;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Access to the sheets of the c0de dimension through the line model of {@code core}. */
public final class LevelSheet {
    private LevelSheet() {}

    /** True if the position is on a sheet of the c0de dimension. */
    public static boolean isOnSheet(Level level, BlockPos pos) {
        return level.dimension().equals(C0deDimension.LEVEL) && SheetGeometry.isOnSheet(pos.getX(), pos.getZ());
    }

    /**
     * A view of the sheet that contains the position, at the height of the position. Blocks in chunks that are
     * not loaded are seen as air.
     */
    public static SheetView view(Level level, BlockPos pos) {
        int sheet = SheetGeometry.sheetIndex(pos.getX(), pos.getZ());
        int y = pos.getY();
        return (x, z) -> {
            if (sheet < 0 || x < 0 || x >= SheetGeometry.SHEET_WIDTH || z < 0) {
                return null;
            }
            BlockPos at = new BlockPos(SheetGeometry.absoluteX(sheet, x), y, z);
            return level.isLoaded(at) ? toCore(level, at) : null;
        };
    }

    /** The c0de block at a position in the line model, or null for air and other blocks. */
    public static Block toCore(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof CodeBlock codeBlock)) {
            return null;
        }
        if (codeBlock.type().keyword() != null) {
            return new Block.KeywordBlock(codeBlock.type().keyword());
        }
        return level.getBlockEntity(pos) instanceof PropertiesBlockEntity entity
                ? entity.toCore(ZoneId.systemDefault())
                : null;
    }
}
