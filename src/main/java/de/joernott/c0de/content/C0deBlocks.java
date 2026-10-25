package de.joernott.c0de.content;

import de.joernott.c0de.block.BlackBedrockBlock;
import de.joernott.c0de.block.CodeBlock;
import de.joernott.c0de.block.CommentBlock;
import de.joernott.c0de.block.OperatorBlock;
import de.joernott.c0de.block.PidBlock;
import de.joernott.c0de.block.PropertyBlock;
import de.joernott.c0de.block.TriggerBlock;
import de.joernott.c0de.block.ValidatedCodeBlock;
import de.joernott.c0de.blockentity.LabelBlockEntity;
import de.joernott.c0de.blockentity.LiteralBlockEntity;
import de.joernott.c0de.blockentity.TagBlockEntity;
import de.joernott.c0de.core.sheet.FloorBlock;
import de.joernott.c0de.platform.Platform;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/** The blocks of the mod. */
public final class C0deBlocks {
    public static Supplier<Block> WHITE_C0DE_BEDROCK;
    public static Supplier<Block> LIGHT_GREEN_C0DE_BEDROCK;
    public static Supplier<Block> BLACK_C0DE_BEDROCK;
    public static Supplier<Block> CURSOR_C0DE_BEDROCK;

    public static Supplier<Block> TRIGGER_BLOCK;
    public static Supplier<Block> TIMER_BLOCK;
    public static Supplier<Block> DEBUG_TRIGGER_BLOCK;
    public static Supplier<Block> PID_BLOCK;
    public static Supplier<Block> DISPLAY_BLOCK;
    public static Supplier<Block> IO_BLOCK;
    public static Supplier<Block> LAPTOP;
    public static Supplier<Block> DISK_DRIVE;

    /** The language blocks. */
    public static final Map<CodeBlockType, Supplier<Block>> CODE_BLOCKS = new EnumMap<>(CodeBlockType.class);

    private C0deBlocks() {}

    public static void register() {
        WHITE_C0DE_BEDROCK = register("white_c0de_bedrock", () -> new Block(bedrock(MapColor.SNOW)));
        LIGHT_GREEN_C0DE_BEDROCK =
                register("light_green_c0de_bedrock", () -> new Block(bedrock(MapColor.COLOR_LIGHT_GREEN)));
        BLACK_C0DE_BEDROCK = register("black_c0de_bedrock", () -> new BlackBedrockBlock(bedrock(MapColor.COLOR_BLACK)));
        CURSOR_C0DE_BEDROCK = register("cursor_c0de_bedrock", () -> new Block(bedrock(MapColor.COLOR_GRAY)));
        TRIGGER_BLOCK = register("trigger_block", () -> new TriggerBlock(trigger(), TriggerBlock.Kind.TRIGGER));
        TIMER_BLOCK = register("timer_block", () -> new TriggerBlock(trigger(), TriggerBlock.Kind.TIMER));
        DEBUG_TRIGGER_BLOCK =
                register("debug_trigger_block", () -> new TriggerBlock(trigger(), TriggerBlock.Kind.DEBUG));
        PID_BLOCK = register("pid_block", () -> new PidBlock(bedrock(MapColor.COLOR_BLACK)));
        DISPLAY_BLOCK = register("display_block", () -> new de.joernott.c0de.io.DisplayBlock(device()));
        IO_BLOCK = register("io_block", () -> new de.joernott.c0de.io.IoBlock(device()));
        LAPTOP = register("laptop", () -> new de.joernott.c0de.io.LaptopBlock(device()));
        DISK_DRIVE = register("disk_drive", () -> new de.joernott.c0de.disk.DiskDriveBlock(device()));
        for (CodeBlockType type : CodeBlockType.values()) {
            CODE_BLOCKS.put(type, register(type.id(), () -> codeBlock(type)));
        }
    }

    /** The language block of a type. */
    public static Block get(CodeBlockType type) {
        return CODE_BLOCKS.get(type).get();
    }

    private static Block codeBlock(CodeBlockType type) {
        // Code can't be moved by pistons or destroyed by explosions, so running programs stay unchanged
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_GRAY)
                .strength(0.6F, 3600000.0F)
                .pushReaction(PushReaction.BLOCK)
                .sound(type == CodeBlockType.NOOP ? SoundType.METAL : SoundType.STONE);
        // The language blocks are as high as a slab, some have other shapes: none of them hides its neighbors
        properties = properties
                .noOcclusion()
                .isValidSpawn((state, level, pos, entityType) -> false)
                .isRedstoneConductor((state, level, pos) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false);
        return switch (type) {
            case COMMENT -> new CommentBlock(properties);
            case LABEL -> new PropertyBlock(properties, type, LabelBlockEntity::new);
            case LITERAL -> new PropertyBlock(properties, type, LiteralBlockEntity::new);
            case OPERATOR -> new OperatorBlock(properties);
            case TAG -> new PropertyBlock(properties, type, TagBlockEntity::new);
            default -> type.isValidated() ? new ValidatedCodeBlock(properties, type) : new CodeBlock(properties, type);
        };
    }

    /** Properties of the trigger blocks: transparent copper and glass. */
    /** Properties of the devices (display, I/O block, laptop): iron. */
    private static BlockBehaviour.Properties device() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F, 6.0F)
                .sound(SoundType.METAL)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties trigger() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_ORANGE)
                .strength(3.0F, 6.0F)
                .sound(SoundType.COPPER)
                .noOcclusion()
                .isRedstoneConductor((state, level, pos) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)
                .requiresCorrectToolForDrops();
    }

    /** The block of the floor of the c0de dimension. */
    public static BlockState floorBlock(FloorBlock block) {
        return switch (block) {
            case WHITE -> WHITE_C0DE_BEDROCK.get().defaultBlockState();
            case GREEN -> LIGHT_GREEN_C0DE_BEDROCK.get().defaultBlockState();
            case BLACK -> BLACK_C0DE_BEDROCK.get().defaultBlockState();
            case CURSOR -> CURSOR_C0DE_BEDROCK.get().defaultBlockState();
        };
    }

    /** Properties of the floor blocks: unbreakable, no drops, not movable by pistons, no mob spawns. */
    private static BlockBehaviour.Properties bedrock(MapColor color) {
        return BlockBehaviour.Properties.of()
                .mapColor(color)
                .strength(-1.0F, 3600000.0F)
                .noLootTable()
                .pushReaction(PushReaction.BLOCK)
                .isValidSpawn((state, level, pos, entityType) -> false)
                .sound(SoundType.STONE);
    }

    private static Supplier<Block> register(String name, Supplier<Block> factory) {
        return Platform.INSTANCE.register(BuiltInRegistries.BLOCK, name, factory);
    }
}
