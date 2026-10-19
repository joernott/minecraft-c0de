package de.joernott.c0de.item;

import de.joernott.c0de.block.TriggerBlock;
import de.joernott.c0de.dimension.C0deDimension;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/**
 * The c0de wrench. Right-clicking a trigger block with it teleports the player into the c0de
 * dimension. Moving code follows in sprint 9.
 */
public class WrenchItem extends Item {
    public WrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel().getBlockState(context.getClickedPos()).getBlock() instanceof TriggerBlock)) {
            return InteractionResult.PASS;
        }
        if (context.getPlayer() instanceof ServerPlayer player) {
            C0deDimension.enter(player, context.getClickedPos());
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }
}
