package de.joernott.c0de.io;

import de.joernott.c0de.compat.Compat;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The display block (docs/concept/19-input-output.md): its front shows the screen of its wall. Right-clicking it
 * with an empty hand opens its dialog with the binding and the camouflage slot, whose block covers its other
 * sides. The wrench turns it (ToolActions).
 */
public class DisplayBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final BooleanProperty CAMOUFLAGED = BooleanProperty.create("camouflaged");
    private static final MapCodec<DisplayBlock> CODEC = simpleCodec(DisplayBlock::new);

    public DisplayBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(CAMOUFLAGED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, CAMOUFLAGED);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DisplayBlockEntity(pos, state);
    }

    /** A display block next to another one takes its direction; otherwise its front faces the player. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        for (Direction direction : Direction.values()) {
            BlockState neighbor = level.getBlockState(context.getClickedPos().relative(direction));
            if (neighbor.getBlock() instanceof DisplayBlock) {
                return defaultBlockState().setValue(FACING, neighbor.getValue(FACING));
            }
        }
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        boolean turned = oldState.getBlock() instanceof DisplayBlock && oldState.getValue(FACING) != state.getValue(FACING);
        if (level instanceof ServerLevel serverLevel && (!oldState.is(this) || turned)) {
            DisplayWalls.changed(serverLevel, pos, null, null);
        }
    }

    /** What happens after the block was removed: its block entity has already cleaned up (removed). */
    private void afterRemoval(ServerLevel level, BlockPos pos) {
        DisplayWalls.removed(level, pos);
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

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof DisplayBlockEntity entity) {
            entity.openMenu(serverPlayer);
        }
        return Compat.success(level);
    }

    /** Turns the block around the axis through a face (left click with the wrench). */
    public static void turn(ServerLevel level, BlockPos pos, Direction face) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof DisplayBlock)) {
            return;
        }
        Direction facing = state.getValue(FACING);
        if (facing.getAxis() == face.getAxis()) {
            return;
        }
        Direction turned = face.getAxisDirection() == Direction.AxisDirection.POSITIVE
                ? facing.getClockWise(face.getAxis())
                : facing.getCounterClockWise(face.getAxis());
        level.setBlock(pos, state.setValue(FACING, turned), Block.UPDATE_ALL);
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return false;
    }

    @Override
    //? if >=1.21.2 {
    /*protected boolean propagatesSkylightDown(BlockState state) {
    *///?} else {
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
    //?}
        return false;
    }
}
