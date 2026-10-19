package de.joernott.c0de.content;

import de.joernott.c0de.block.BlackBedrockBlock;
import de.joernott.c0de.block.TriggerBlock;
import de.joernott.c0de.core.sheet.FloorBlock;
import de.joernott.c0de.platform.Platform;
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

    /** Provisional trigger block: only used to enter the c0de dimension until sprint 8. */
    public static Supplier<Block> TRIGGER_BLOCK;

    private C0deBlocks() {}

    public static void register() {
        WHITE_C0DE_BEDROCK = register("white_c0de_bedrock", () -> new Block(bedrock(MapColor.SNOW)));
        LIGHT_GREEN_C0DE_BEDROCK =
                register("light_green_c0de_bedrock", () -> new Block(bedrock(MapColor.COLOR_LIGHT_GREEN)));
        BLACK_C0DE_BEDROCK = register("black_c0de_bedrock", () -> new BlackBedrockBlock(bedrock(MapColor.COLOR_BLACK)));
        CURSOR_C0DE_BEDROCK = register("cursor_c0de_bedrock", () -> new Block(bedrock(MapColor.COLOR_GRAY)));
        TRIGGER_BLOCK = register("trigger_block", () -> new TriggerBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_ORANGE)
                .strength(3.0F, 6.0F)
                .sound(SoundType.COPPER)
                .requiresCorrectToolForDrops()));
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
