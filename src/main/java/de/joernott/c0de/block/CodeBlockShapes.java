package de.joernott.c0de.block;

import de.joernott.c0de.content.CodeBlockType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The shapes of the language blocks that are not cubes. They MUST match the block models written by
 * scripts/generate_content.py (SHAPES): PARAMETER is a hopper, RESULT an upside-down hopper, BEGIN a trapezoid of
 * four steps that is wide at the bottom and END the same trapezoid upside down.
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
    private static final VoxelShape STEPS = steps(false);
    private static final VoxelShape STEPS_UPSIDE_DOWN = steps(true);

    private CodeBlockShapes() {}

    private static VoxelShape steps(boolean upsideDown) {
        VoxelShape shape = Shapes.empty();
        for (int i = 0; i < 4; i++) {
            double inset = 1.5 * i;
            double bottom = upsideDown ? 12 - 4 * i : 4 * i;
            shape = Shapes.or(shape, Block.box(inset, bottom, inset, 16 - inset, bottom + 4, 16 - inset));
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
            case BEGIN -> STEPS;
            case END -> STEPS_UPSIDE_DOWN;
            default -> Shapes.block();
        };
    }
}
