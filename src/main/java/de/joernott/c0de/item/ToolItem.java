package de.joernott.c0de.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The text marker and the hammer: tools whose left click acts on lines of a sheet instead of breaking blocks
 * (see {@link de.joernott.c0de.tool.ToolActions}).
 */
public class ToolItem extends Item {
    public ToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return false;
    }
}
