package de.joernott.c0de.blockentity;

import de.joernott.c0de.compat.Nbt;
import de.joernott.c0de.content.C0deBlockEntities;
import de.joernott.c0de.core.edit.PropertyRules;
import de.joernott.c0de.core.line.Block;
import java.time.ZoneId;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The property of a COMMENT: its text (docs/concept/02-language-basics.md). The text is shown above the block
 * while a player looks at it, not on its sides.
 */
public class CommentBlockEntity extends PropertiesBlockEntity {
    private String text = "";

    public CommentBlockEntity(BlockPos pos, BlockState state) {
        super(C0deBlockEntities.COMMENT.get(), pos, state);
    }

    public String text() {
        return text;
    }

    @Override
    public String dialog() {
        return "comment";
    }

    @Override
    public Block toCore(ZoneId zone) {
        return new Block.CommentBlock(text);
    }

    @Override
    public String displayText() {
        return "";
    }

    @Override
    protected Optional<String> check(CompoundTag properties, ServerLevel level) {
        return PropertyRules.comment(Nbt.string(properties, "text"));
    }

    @Override
    protected void read(CompoundTag tag) {
        text = Nbt.string(tag, "text");
    }

    @Override
    protected void write(CompoundTag tag) {
        tag.putString("text", text);
    }
}
