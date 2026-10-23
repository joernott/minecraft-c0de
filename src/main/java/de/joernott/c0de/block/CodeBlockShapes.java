package de.joernott.c0de.block;

import de.joernott.c0de.content.CodeBlockType;
import java.util.function.DoubleUnaryOperator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The outline and collision shapes of the language blocks. They MUST match the block models: all blocks are as
 * high as a slab; PARAMETER is a hopper and RESULT an upside-down hopper at half their size, VARIABLE (chest) and
 * CONSTANT (barrel) are half as wide as a block, OPERATOR is a flat symbol (scripts/block_models.py). BEGIN, END,
 * PROGRAM and LITERAL get their shapes from {@code ExtrudedModel} on the client; their diagonal and round sides
 * are approximated with slices.
 */
public final class CodeBlockShapes {
    /** The height of the language blocks in pixels. */
    public static final double HEIGHT = 8;
    /** The height of the extruded operator symbols in pixels. */
    public static final double OPERATOR_HEIGHT = 4;
    /** How far the narrow end of BEGIN and END is moved inwards on each side, in pixels. */
    public static final double TRAPEZOID_INSET = 4;
    /** The length of the cut corners of the octagon of LITERAL, in pixels: a regular octagon. */
    public static final double OCTAGON_CUT = 16 / (2 + Math.sqrt(2));

    private static final VoxelShape SLAB = Block.box(0, 0, 0, 16, HEIGHT, 16);
    private static final VoxelShape HOPPER = Shapes.or(
            Shapes.join(Block.box(4, 5, 4, 12, 8, 12), Block.box(5, 5.5, 5, 11, 8, 11), BooleanOp.ONLY_FIRST),
            Block.box(6, 2, 6, 10, 5, 10),
            Block.box(7, 0, 7, 9, 2, 9));
    private static final VoxelShape HOPPER_UPSIDE_DOWN = Shapes.or(
            Shapes.join(Block.box(4, 0, 4, 12, 3, 12), Block.box(5, 0, 5, 11, 2.5, 11), BooleanOp.ONLY_FIRST),
            Block.box(6, 3, 6, 10, 6, 10),
            Block.box(7, 6, 7, 9, 8, 9));
    private static final VoxelShape HALF_WIDTH = Block.box(4, 0, 4, 12, HEIGHT, 12);
    private static final VoxelShape FLAT = Block.box(0, 0, 0, 16, OPERATOR_HEIGHT, 16);
    private static final VoxelShape TRAPEZOID_NARROW_IN_NORTH =
            slices(z -> TRAPEZOID_INSET * (1 - z / 16), z -> 16 - TRAPEZOID_INSET * (1 - z / 16));
    private static final VoxelShape TRAPEZOID_NARROW_IN_SOUTH =
            slices(z -> TRAPEZOID_INSET * z / 16, z -> 16 - TRAPEZOID_INSET * z / 16);
    /** PROGRAM: a semicircle in the west and a rectangle in the east. */
    private static final VoxelShape HALF_ROUND = slices(z -> 8 - Math.sqrt(64 - (z - 8) * (z - 8)), z -> 16);
    private static final VoxelShape OCTAGON = slices(
            z -> Math.max(0, OCTAGON_CUT - Math.min(z, 16 - z)),
            z -> 16 - Math.max(0, OCTAGON_CUT - Math.min(z, 16 - z)));

    private CodeBlockShapes() {}

    /** A shape of slices 2 pixels deep, from west to east as given for the z coordinate in the middle of each. */
    private static VoxelShape slices(DoubleUnaryOperator west, DoubleUnaryOperator east) {
        VoxelShape shape = Shapes.empty();
        for (int z = 0; z < 16; z += 2) {
            double middle = z + 1;
            shape = Shapes.or(
                    shape, Block.box(west.applyAsDouble(middle), 0, z, east.applyAsDouble(middle), HEIGHT, z + 2));
        }
        return shape;
    }

    /** The shape of a language block. */
    public static VoxelShape of(CodeBlockType type) {
        return switch (type) {
            case PARAMETER -> HOPPER;
            case RESULT -> HOPPER_UPSIDE_DOWN;
            case BEGIN -> TRAPEZOID_NARROW_IN_NORTH;
            case END -> TRAPEZOID_NARROW_IN_SOUTH;
            case PROGRAM -> HALF_ROUND;
            case LITERAL -> OCTAGON;
            case VARIABLE, CONSTANT -> HALF_WIDTH;
            case OPERATOR -> FLAT;
            default -> SLAB;
        };
    }
}
