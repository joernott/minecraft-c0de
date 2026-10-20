package de.joernott.c0de.content;

import de.joernott.c0de.blockentity.CommentBlockEntity;
import de.joernott.c0de.blockentity.LabelBlockEntity;
import de.joernott.c0de.blockentity.LiteralBlockEntity;
import de.joernott.c0de.blockentity.OperatorBlockEntity;
import de.joernott.c0de.blockentity.PropertiesBlockEntity;
import de.joernott.c0de.blockentity.TagBlockEntity;
import de.joernott.c0de.platform.Platform;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** The block entity types of the blocks with properties. */
public final class C0deBlockEntities {
    public static Supplier<BlockEntityType<LabelBlockEntity>> LABEL;
    public static Supplier<BlockEntityType<LiteralBlockEntity>> LITERAL;
    public static Supplier<BlockEntityType<OperatorBlockEntity>> OPERATOR;
    public static Supplier<BlockEntityType<CommentBlockEntity>> COMMENT;
    public static Supplier<BlockEntityType<TagBlockEntity>> TAG;

    private C0deBlockEntities() {}

    public static void register() {
        LABEL = register("label", LabelBlockEntity::new, CodeBlockType.LABEL);
        LITERAL = register("literal", LiteralBlockEntity::new, CodeBlockType.LITERAL);
        OPERATOR = register("operator", OperatorBlockEntity::new, CodeBlockType.OPERATOR);
        COMMENT = register("comment", CommentBlockEntity::new, CodeBlockType.COMMENT);
        TAG = register("tag", TagBlockEntity::new, CodeBlockType.TAG);
    }

    /** All block entity types, e.g. for registering their renderers. */
    public static List<BlockEntityType<? extends PropertiesBlockEntity>> all() {
        return List.of(LABEL.get(), LITERAL.get(), OPERATOR.get(), COMMENT.get(), TAG.get());
    }

    private static <T extends BlockEntity> Supplier<BlockEntityType<T>> register(
            String name, BiFunction<BlockPos, BlockState, T> factory, CodeBlockType block) {
        return Platform.INSTANCE.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                name,
                () -> Platform.INSTANCE.blockEntityType(factory, C0deBlocks.get(block)));
    }
}
