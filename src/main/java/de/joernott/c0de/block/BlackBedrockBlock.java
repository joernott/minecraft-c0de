package de.joernott.c0de.block;

import de.joernott.c0de.compat.Compat;
import de.joernott.c0de.dimension.C0deDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Black c0de bedrock. Right-clicking it with an empty hand in the c0de dimension takes the player back
 * to where they entered the dimension.
 */
public class BlackBedrockBlock extends Block {
    public BlackBedrockBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        // Called with any item that doesn't interact with blocks, so the hand is checked explicitly
        if (!level.dimension().equals(C0deDimension.LEVEL) || !player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            C0deDimension.leave(serverPlayer);
        }
        return Compat.success(level);
    }
}
