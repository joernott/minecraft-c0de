package de.joernott.c0de.io;

import com.mojang.serialization.MapCodec;
import de.joernott.c0de.dialog.DialogService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The display block (docs/concept/19-input-output.md): its front shows the screen of its wall. Right-clicking it
 * with an empty hand opens the binding dialog; right-clicking it with a block camouflages its other sides, and
 * sneaking with an empty hand removes the camouflage. The wrench turns it (ToolActions).
 */
public class DisplayBlock extends BaseEntityBlock implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
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

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.is(newState.getBlock())) {
            super.onRemove(state, level, pos, newState, movedByPiston);
            return;
        }
        BlockPos removedOrigin = null;
        CompoundTag removedScreen = null;
        if (level.getBlockEntity(pos) instanceof DisplayBlockEntity entity) {
            if (entity.isOrigin()) {
                removedOrigin = pos;
                removedScreen = entity.screen() == null ? null : ScreenNbt.write(entity.screen());
            }
            if (entity.camouflage() != null) {
                Block.popResource(level, pos, new ItemStack(entity.camouflage()));
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (level instanceof ServerLevel serverLevel) {
            if (removedOrigin != null) {
                IoDevices devices = IoDevices.get(serverLevel.getServer());
                // The wall that remains takes the place of the removed origin in the order of the devices
                DisplayWalls.changed(serverLevel, pos, removedOrigin, removedScreen);
                devices.remove(serverLevel.dimension(), removedOrigin);
            } else {
                DisplayWalls.changed(serverLevel, pos, null, null);
            }
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {
        if (stack.getItem() instanceof BlockItem item
                && !(item.getBlock() instanceof EntityBlock)
                && item.getBlock().defaultBlockState().isCollisionShapeFullBlock(level, pos)
                && !state.getValue(CAMOUFLAGED)) {
            if (!level.isClientSide() && level.getBlockEntity(pos) instanceof DisplayBlockEntity entity) {
                entity.setCamouflage(item.getBlock());
                level.setBlock(pos, state.setValue(CAMOUFLAGED, true), Block.UPDATE_ALL);
                stack.consume(1, player);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        return stack.isEmpty()
                ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
                : ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.isShiftKeyDown() && state.getValue(CAMOUFLAGED)) {
            if (!level.isClientSide() && level.getBlockEntity(pos) instanceof DisplayBlockEntity entity) {
                Block.popResource(level, pos.relative(hitResult.getDirection()), new ItemStack(entity.camouflage()));
                entity.setCamouflage(null);
                level.setBlock(pos, state.setValue(CAMOUFLAGED, false), Block.UPDATE_ALL);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (player instanceof ServerPlayer serverPlayer) {
            DialogService.open(serverPlayer, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
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
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }
}
