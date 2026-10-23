package de.joernott.c0de.block;

import de.joernott.c0de.content.CodeBlockType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The shapes of the language blocks that are not cubes. They MUST match the block models: PARAMETER is a hopper
 * and RESULT an upside-down hopper (scripts/generate_content.py, SHAPES). Seen from above, BEGIN is a trapezoid
 * that is narrow in the north and END one that is narrow in the south ({@code TrapezoidModel} on the client);
 * their shapes approximate the diagonals with four slices.
 */
public final class CodeBlockShapes {
    private static final VoxelShape HOPPER = Shapes.or(
            Shapes.join(
                    Block.box(0, 10, 0, 16, 16, 16), Block.box(2, 11, 2, 14, 16, 14), BooleanOp.ONLY_FIRST),
            Block.box(4, 4, 4, 12, 10, 12),
            Block.box(6, 0, 6, 10, 4, 10));
    private static final VoxelShape HOPPER_UPSIDE_DOWN = Shapes.or(
            Shapes.join(Block.box(0, 0, 0, 16, 6, 16), Block.box(2, 0, 2, 14, 5, 14), BooleanOp.ONLY_FIRST),
            Block.box(4, 6, 4, 12, 12, 12),
            Block.box(6, 12, 6, 10, 16, 10));
    /** How far the narrow end of BEGIN and END is moved inwards on each side, in pixels (TrapezoidModel.INSET). */
    private static final double INSET = 4;

    private static final VoxelShape TRAPEZOID_NARROW_IN_NORTH = trapezoid(true);
    private static final VoxelShape TRAPEZOID_NARROW_IN_SOUTH = trapezoid(false);

    private CodeBlockShapes() {}

    private static VoxelShape trapezoid(boolean narrowInNorth) {
        VoxelShape shape = Shapes.empty();
        for (int i = 0; i < 4; i++) {
            // The width in the middle of the slice
            double z = (4 * i + 2) / 16.0;
            double inset = INSET * (narrowInNorth ? 1 - z : z);
            shape = Shapes.or(shape, Block.box(inset, 0, 4 * i, 16 - inset, 16, 4 * i + 4));
        }
        return shape;
    }

    /** True for the blocks that are not cubes; they MUST NOT hide the faces of their neighbors. */
    public static boolean isShaped(CodeBlockType type) {
        return type == CodeBlockType.PARAMETER
                || type == CodeBlockType.RESULT
                || type == CodeBlockType.BEGIN
                || type == CodeBlockType.END;
    }

    /** The shape of a language block. */
    public static VoxelShape of(CodeBlockType type) {
        return switch (type) {
            case PARAMETER -> HOPPER;
            case RESULT -> HOPPER_UPSIDE_DOWN;
            case BEGIN -> TRAPEZOID_NARROW_IN_NORTH;
            case END -> TRAPEZOID_NARROW_IN_SOUTH;
            default -> Shapes.block();
        };
    }
}
