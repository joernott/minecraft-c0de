package de.joernott.c0de.item;

import de.joernott.c0de.compat.Compat;
import de.joernott.c0de.tool.ToolActions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The text marker, the hammer and the pliers: tools whose left and right clicks act on lines of a sheet instead of
 * breaking and placing blocks (see {@link ToolActions}).
 */
public class ToolItem extends Item {
    public ToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!ToolActions.isOnLine(context.getLevel(), context.getClickedPos())) {
            return InteractionResult.PASS;
        }
        if (context.getPlayer() instanceof ServerPlayer player
                && !ToolActions.rightClick(player, Compat.level(player), context.getClickedPos())) {
            return InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    //? if >=1.21.5 {
    /*@Override
    public boolean canDestroyBlock(
            ItemStack stack, BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.LivingEntity entity) {
        return false;
    }
    *///?} else {
    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return false;
    }
    //?}
}
