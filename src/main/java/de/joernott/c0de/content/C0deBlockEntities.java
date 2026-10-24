package de.joernott.c0de.content;

import de.joernott.c0de.blockentity.CommentBlockEntity;
import de.joernott.c0de.blockentity.LabelBlockEntity;
import de.joernott.c0de.blockentity.LiteralBlockEntity;
import de.joernott.c0de.blockentity.OperatorBlockEntity;
import de.joernott.c0de.blockentity.PidBlockEntity;
import de.joernott.c0de.blockentity.PropertiesBlockEntity;
import de.joernott.c0de.blockentity.TagBlockEntity;
import de.joernott.c0de.blockentity.TriggerBlockEntity;
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
    public static Supplier<BlockEntityType<TriggerBlockEntity>> TRIGGER;
    public static Supplier<BlockEntityType<PidBlockEntity>> PID;
    public static Supplier<BlockEntityType<de.joernott.c0de.io.DisplayBlockEntity>> DISPLAY;
    public static Supplier<BlockEntityType<de.joernott.c0de.io.IoBlockEntity>> IO;
    public static Supplier<BlockEntityType<de.joernott.c0de.io.LaptopBlockEntity>> LAPTOP;

    private C0deBlockEntities() {}

    public static void register() {
        LABEL = register("label", LabelBlockEntity::new, CodeBlockType.LABEL);
        LITERAL = register("literal", LiteralBlockEntity::new, CodeBlockType.LITERAL);
        OPERATOR = register("operator", OperatorBlockEntity::new, CodeBlockType.OPERATOR);
        COMMENT = register("comment", CommentBlockEntity::new, CodeBlockType.COMMENT);
        TAG = register("tag", TagBlockEntity::new, CodeBlockType.TAG);
        TRIGGER = Platform.INSTANCE.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                "trigger",
                () -> Platform.INSTANCE.blockEntityType(
                        TriggerBlockEntity::new,
                        C0deBlocks.TRIGGER_BLOCK.get(),
                        C0deBlocks.TIMER_BLOCK.get(),
                        C0deBlocks.DEBUG_TRIGGER_BLOCK.get()));
        // Hoppers, pipes and cables fill the input and empty the output of trigger blocks
        Platform.INSTANCE.exposeContainer(TRIGGER);
        PID = Platform.INSTANCE.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                "pid",
                () -> Platform.INSTANCE.blockEntityType(PidBlockEntity::new, C0deBlocks.PID_BLOCK.get()));
        DISPLAY = Platform.INSTANCE.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                "display",
                () -> Platform.INSTANCE.blockEntityType(
                        de.joernott.c0de.io.DisplayBlockEntity::new, C0deBlocks.DISPLAY_BLOCK.get()));
        IO = Platform.INSTANCE.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                "io",
                () -> Platform.INSTANCE.blockEntityType(de.joernott.c0de.io.IoBlockEntity::new, C0deBlocks.IO_BLOCK.get()));
        // Like trigger blocks, hoppers and pipes fill the input and empty the output of I/O blocks
        Platform.INSTANCE.exposeContainer(IO);
        LAPTOP = Platform.INSTANCE.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                "laptop",
                () -> Platform.INSTANCE.blockEntityType(
                        de.joernott.c0de.io.LaptopBlockEntity::new, C0deBlocks.LAPTOP.get()));
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
