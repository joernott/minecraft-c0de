package de.joernott.c0de.block;

import de.joernott.c0de.compat.Compat;
import de.joernott.c0de.content.CodeBlockType;
import de.joernott.c0de.core.edit.PlacementRules;
import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.dialog.SheetProtection;
import de.joernott.c0de.sheet.LevelSheet;
import de.joernott.c0de.sheet.SheetValidation;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
//? if <1.21.2
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A language block of c0de: a keyword, type, NOOP or a block with properties. */
public class CodeBlock extends Block {
    private final CodeBlockType type;

    public CodeBlock(Properties properties, CodeBlockType type) {
        super(properties);
        this.type = type;
    }

    public CodeBlockType type() {
        return type;
    }

    /**
     * Checks the placement rules on the code layer of a sheet. A block that breaks them is not placed, and the
     * player gets the reason.
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (state == null || !LevelSheet.isOnSheet(level, pos)) {
            return state;
        }
        Optional<Component> running = SheetProtection.reason(context.getPlayer(), level, pos, true);
        if (running.isPresent()) {
            if (context.getPlayer() != null) {
                Compat.message(context.getPlayer(), running.get(), true);
            }
            return null;
        }
        int x = SheetGeometry.relativeX(pos.getX());
        Optional<String> problem = PlacementRules.checkArea(x, pos.getY())
                .or(() -> PlacementRules.check(LevelSheet.view(level, pos), type.placed(), x, pos.getZ()));
        if (problem.isEmpty()) {
            return state;
        }
        if (!level.isClientSide() && context.getPlayer() != null) {
            Compat.message(context.getPlayer(), Component.translatable("message.c0de.not_placed", problem.get()), true);
        }
        return null;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CodeBlockShapes.of(type);
    }

    /** A new block on a sheet is validated with its program. */
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        // Changes of the block state valid itself don't need a validation
        if (!oldState.is(state.getBlock())) {
            SheetValidation.changed(level, pos);
        }
    }

    /** What happens after the block was removed: its block entity has already cleaned up (removed). */
    private void afterRemoval(ServerLevel level, BlockPos pos) {
        SheetValidation.changed(level, pos);
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

    /**
     * Right-clicking with an item uses the item, e.g. places a block next to this one. Only an empty hand
     * interacts with the block itself.
     */
    //? if >=1.21.2 {
    /*@Override
    protected net.minecraft.world.InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {
        return stack.isEmpty()
                ? net.minecraft.world.InteractionResult.TRY_WITH_EMPTY_HAND
                : net.minecraft.world.InteractionResult.PASS;
    }
    *///?} else {
    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {
        return stack.isEmpty()
                ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
                : ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
    }
    //?}
}
