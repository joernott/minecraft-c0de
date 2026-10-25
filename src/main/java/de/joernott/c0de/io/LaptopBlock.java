package de.joernott.c0de.io;

import de.joernott.c0de.compat.Compat;
import com.mojang.serialization.MapCodec;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The laptop (docs/concept/19-input-output.md): an open laptop whose screen faces the player who placed it.
 * Right-clicking it opens its terminal, with the screen, the input line and the binding. The wrench turns it.
 */
public class LaptopBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    //? if <26.3
    private static final MapCodec<LaptopBlock> CODEC = simpleCodec(LaptopBlock::new);
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    static {
        // The keyboard in front of the screen, the lid at the back
        SHAPES.put(Direction.NORTH, Shapes.or(box(1, 0, 1, 15, 1, 13), box(1, 1, 13, 15, 11, 14)));
        SHAPES.put(Direction.SOUTH, Shapes.or(box(1, 0, 3, 15, 1, 15), box(1, 1, 2, 15, 11, 3)));
        SHAPES.put(Direction.WEST, Shapes.or(box(1, 0, 1, 13, 1, 15), box(13, 1, 1, 14, 11, 15)));
        SHAPES.put(Direction.EAST, Shapes.or(box(3, 0, 1, 15, 1, 15), box(2, 1, 1, 3, 11, 15)));
    }

    public LaptopBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    //? if <26.3 {
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    //?}

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LaptopBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof LaptopBlockEntity laptop) {
            Terminals terminals = Terminals.get(Compat.server(serverPlayer));
            terminals.open(serverPlayer, terminals.terminal(laptop.terminal()));
        }
        return Compat.success(level);
    }

    /** Turns the laptop clockwise, seen from above (left click with the wrench). */
    public static void turn(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof LaptopBlock) {
            level.setBlock(pos, state.setValue(FACING, state.getValue(FACING).getClockWise()), Block.UPDATE_ALL);
        }
    }

    /** What happens after the block was removed: its block entity has already cleaned up (removed). */
    private void afterRemoval(ServerLevel level, BlockPos pos) {
    }

    //? if >=1.21.5 {
    /*@Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        afterRemoval(level, pos);
    }
    *///?} else {
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        boolean replaced = !state.is(newState.getBlock());
        if (replaced && level.getBlockEntity(pos) instanceof de.joernott.c0de.compat.C0deBlockEntity entity) {
            entity.removedBeforeRemoval();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (replaced && level instanceof ServerLevel serverLevel) {
            afterRemoval(serverLevel, pos);
        }
    }
    //?}
}
