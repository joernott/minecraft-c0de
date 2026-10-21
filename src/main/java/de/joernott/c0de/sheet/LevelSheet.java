package de.joernott.c0de.sheet;

import de.joernott.c0de.block.CodeBlock;
import de.joernott.c0de.blockentity.PropertiesBlockEntity;
import de.joernott.c0de.core.edit.SheetView;
import de.joernott.c0de.core.line.Block;
import de.joernott.c0de.core.line.Line;
import de.joernott.c0de.core.line.Sheet;
import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.dimension.C0deDimension;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Access to the sheets of the c0de dimension through the line model of {@code core}. */
public final class LevelSheet {
    /** The maximum number of lines that are read, so that very long sheets can't stall the server. */
    public static final int MAX_LINES = 10_000;

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

    /** The index of the sheet that contains the position. */
    public static int sheetOf(BlockPos pos) {
        return SheetGeometry.sheetIndex(pos.getX(), pos.getZ());
    }

    /**
     * Reads the program of a sheet: the lines from z=0 to the first empty line, which ends the program.
     *
     * @return the lines, or empty if a part of the program is in a chunk that is not loaded
     */
    public static Optional<Sheet> read(Level level, int sheet) {
        return readGroup(level, sheet, 0).map(Sheet::new);
    }

    /**
     * Reads consecutive lines that are not empty, starting at a line.
     *
     * @return the lines, or empty if one of them is in a chunk that is not loaded
     */
    public static Optional<List<Line>> readGroup(Level level, int sheet, int fromZ) {
        List<Line> lines = new ArrayList<>();
        for (int z = fromZ; z < fromZ + MAX_LINES; z++) {
            if (!isLoaded(level, sheet, z)) {
                return Optional.empty();
            }
            Line line = readLine(level, sheet, z);
            if (line.isEmpty()) {
                return Optional.of(lines);
            }
            lines.add(line);
        }
        return Optional.of(lines);
    }

    /** True if the code area of a line is in loaded chunks. */
    public static boolean isLoaded(Level level, int sheet, int z) {
        return level.isLoaded(codePos(sheet, SheetGeometry.CODE_MIN_X, z))
                && level.isLoaded(codePos(sheet, SheetGeometry.CODE_MAX_X, z));
    }

    /** The position of the PID block of a process: on the cursor rail next to the PROGRAM line. */
    public static BlockPos pidPos(int sheet, int pid) {
        return new BlockPos(SheetGeometry.absoluteX(sheet, SheetGeometry.CURSOR_X), pid, 0);
    }

    /** The position of a block of the code layer of a sheet. */
    public static BlockPos codePos(int sheet, int relativeX, int z) {
        return new BlockPos(SheetGeometry.absoluteX(sheet, relativeX), SheetGeometry.CODE_Y, z);
    }

    /** Reads one line of a sheet; its chunks must be loaded. */
    public static Line readLine(Level level, int sheet, int z) {
        List<Line.Cell> cells = new ArrayList<>();
        for (int x = SheetGeometry.CODE_MIN_X; x <= SheetGeometry.CODE_MAX_X; x++) {
            Block block = toCore(level, codePos(sheet, x, z));
            if (block != null) {
                cells.add(new Line.Cell(x, block));
            }
        }
        return new Line(z, cells);
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
