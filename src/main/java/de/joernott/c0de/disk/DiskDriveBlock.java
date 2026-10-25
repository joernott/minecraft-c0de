package de.joernott.c0de.disk;

import de.joernott.c0de.compat.Compat;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
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

/**
 * The disk drive (task 11.11.9): a 3.5" floppy drive in a case, with a slot for one c0de disk. Its dialog saves the
 * program of the disk to the folder {@code c0de} of the Minecraft of the player and loads programs from there onto
 * the disk.
 */
public class DiskDriveBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    //? if <26.3
    private static final MapCodec<DiskDriveBlock> CODEC = simpleCodec(DiskDriveBlock::new);

    public DiskDriveBlock(Properties properties) {
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
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DiskDriveBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof DiskDriveBlockEntity drive) {
            drive.openMenu(serverPlayer);
        }
        return Compat.success(level);
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
