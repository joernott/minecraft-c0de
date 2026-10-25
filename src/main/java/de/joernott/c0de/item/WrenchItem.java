package de.joernott.c0de.item;

import de.joernott.c0de.block.TriggerBlock;
import de.joernott.c0de.dimension.C0deDimension;
import de.joernott.c0de.sheet.SheetOwners;
import de.joernott.c0de.tool.ToolActions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The c0de wrench (docs/concept/14-items-and-tools.md). Right-clicking a trigger block with it teleports the
 * player into the c0de dimension. On a sheet, left-clicking a line moves the marked lines in front of it, and
 * right-clicking a line copies them there.
 */
public class WrenchItem extends Item {
    public WrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (level.getBlockState(pos).getBlock() instanceof TriggerBlock) {
            if (context.getPlayer() instanceof ServerPlayer player) {
                C0deDimension.enter(player, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (SheetOwners.isCorner(level, pos)) {
            // The corner of a sheet: give the sheet to another player (task 11.11.7)
            if (context.getPlayer() instanceof ServerPlayer player) {
                SheetOwners.openGive(player, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (!ToolActions.isOnLine(level, pos)) {
            return InteractionResult.PASS;
        }
        if (context.getPlayer() instanceof ServerPlayer player) {
            ToolActions.wrench(player, player.serverLevel(), pos, true);
        }
        return InteractionResult.SUCCESS;
    }

    /** The wrench doesn't break blocks; its left click moves the marked lines. */
    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return false;
    }
}
