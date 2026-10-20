package de.joernott.c0de.block;

import de.joernott.c0de.content.CodeBlockType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

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
     * Right-clicking with an item uses the item, e.g. places a block next to this one. Only an empty hand
     * interacts with the block itself.
     */
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
}
