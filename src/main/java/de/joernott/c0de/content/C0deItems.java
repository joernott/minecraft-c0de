package de.joernott.c0de.content;

import de.joernott.c0de.item.DebugToolItem;
import de.joernott.c0de.item.DiskItem;
import de.joernott.c0de.item.ToolItem;
import de.joernott.c0de.item.ValueItem;
import de.joernott.c0de.item.WrenchItem;
import de.joernott.c0de.platform.Platform;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/** The items of the mod and its creative mode tab. */
public final class C0deItems {
    public static Supplier<Item> TRIGGER_BLOCK;
    public static Supplier<Item> DISPLAY_BLOCK;
    public static Supplier<Item> IO_BLOCK;
    public static Supplier<Item> LAPTOP;
    public static Supplier<Item> TABLET;
    public static Supplier<Item> LITERAL_ITEM;
    public static Supplier<Item> TIMER_BLOCK;
    public static Supplier<Item> DEBUG_TRIGGER_BLOCK;
    public static Supplier<Item> WRENCH;
    public static Supplier<Item> DEBUG_TOOL;
    public static Supplier<Item> MARKER;
    public static Supplier<Item> HAMMER;
    public static Supplier<Item> DISK;
    public static Supplier<Item> TYPE_BASE;
    public static Supplier<Item> DATA_ITEM;
    public static Supplier<Item> VARIABLE_ITEM;
    public static Supplier<Item> CONSTANT_ITEM;

    /** The type items by type name, e.g. {@code BOOLEAN}. */
    public static final Map<String, Supplier<Item>> TYPE_ITEMS = new java.util.LinkedHashMap<>();

    /** The types of the type items in the order of the concept. */
    public static final java.util.List<String> TYPES = java.util.List.of(
            "BOOLEAN", "INTEGER", "FLOAT", "CHAR", "STRING", "ARRAY", "HASH", "STRUCT", "TIME", "ERROR");

    /** The items of the floor blocks of the c0de dimension. */
    public static final java.util.List<Supplier<Item>> FLOOR = new java.util.ArrayList<>();
    public static Supplier<Item> STRUCTURAL_BASE;

    /** The items of the language blocks. */
    public static final Map<CodeBlockType, Supplier<Item>> CODE_BLOCKS = new EnumMap<>(CodeBlockType.class);
    public static Supplier<CreativeModeTab> TAB;

    private C0deItems() {}

    public static void register() {
        DISPLAY_BLOCK = register(
                "display_block", () -> new BlockItem(C0deBlocks.DISPLAY_BLOCK.get(), new Item.Properties()));
        IO_BLOCK = register("io_block", () -> new BlockItem(C0deBlocks.IO_BLOCK.get(), new Item.Properties()));
        LAPTOP = register("laptop", () -> new BlockItem(C0deBlocks.LAPTOP.get(), new Item.Properties()));
        TABLET = register("tablet", () -> new de.joernott.c0de.item.TabletItem(new Item.Properties().stacksTo(1)));
        TRIGGER_BLOCK = register(
                "trigger_block", () -> new BlockItem(C0deBlocks.TRIGGER_BLOCK.get(), new Item.Properties()));
        TIMER_BLOCK =
                register("timer_block", () -> new BlockItem(C0deBlocks.TIMER_BLOCK.get(), new Item.Properties()));
        DEBUG_TRIGGER_BLOCK = register(
                "debug_trigger_block", () -> new BlockItem(C0deBlocks.DEBUG_TRIGGER_BLOCK.get(), new Item.Properties()));
        WRENCH = register("wrench", () -> new WrenchItem(new Item.Properties().stacksTo(1)));
        DEBUG_TOOL = register("debug_tool", () -> new DebugToolItem(new Item.Properties().stacksTo(1)));
        MARKER = register("marker", () -> new ToolItem(new Item.Properties().stacksTo(1)));
        HAMMER = register("hammer", () -> new ToolItem(new Item.Properties().stacksTo(1)));
        DISK = register("c0de_disk", () -> new DiskItem(new Item.Properties().stacksTo(1)));
        // The floor can be broken in creative mode, so creative players need the blocks to repair it
        // The suppliers of the blocks can't be called before the blocks are registered, so the names are listed
        java.util.Map<String, Supplier<Block>> floor = new java.util.LinkedHashMap<>();
        floor.put("white_c0de_bedrock", C0deBlocks.WHITE_C0DE_BEDROCK);
        floor.put("light_green_c0de_bedrock", C0deBlocks.LIGHT_GREEN_C0DE_BEDROCK);
        floor.put("black_c0de_bedrock", C0deBlocks.BLACK_C0DE_BEDROCK);
        floor.put("cursor_c0de_bedrock", C0deBlocks.CURSOR_C0DE_BEDROCK);
        floor.forEach((name, block) ->
                FLOOR.add(register(name, () -> new BlockItem(block.get(), new Item.Properties()))));
        DATA_ITEM = register("data_item", () -> new Item(new Item.Properties()));
        for (String type : TYPES) {
            TYPE_ITEMS.put(
                    type,
                    register(
                            "type_" + type.toLowerCase(java.util.Locale.ROOT) + "_item",
                            () -> new Item(new Item.Properties())));
        }
        VARIABLE_ITEM = register("variable_item", () -> new ValueItem(new Item.Properties(), false));
        CONSTANT_ITEM = register("constant_item", () -> new ValueItem(new Item.Properties(), true));
        LITERAL_ITEM = register("literal_item", () -> new de.joernott.c0de.item.LiteralItem(new Item.Properties()));
        TYPE_BASE = register("type_base", () -> new Item(new Item.Properties()));
        STRUCTURAL_BASE = register("structural_base", () -> new Item(new Item.Properties()));
        for (CodeBlockType type : CodeBlockType.values()) {
            CODE_BLOCKS.put(
                    type, register(type.id(), () -> new BlockItem(C0deBlocks.get(type), new Item.Properties())));
        }
        TAB = Platform.INSTANCE.register(BuiltInRegistries.CREATIVE_MODE_TAB, "c0de", () -> CreativeModeTab.builder(
                        CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup.c0de"))
                .icon(() -> new ItemStack(WRENCH.get()))
                .displayItems((parameters, output) -> {
                    output.accept(TRIGGER_BLOCK.get());
                    output.accept(DISPLAY_BLOCK.get());
                    output.accept(IO_BLOCK.get());
                    output.accept(LAPTOP.get());
                    output.accept(TABLET.get());
                    output.accept(TIMER_BLOCK.get());
                    output.accept(DEBUG_TRIGGER_BLOCK.get());
                    output.accept(WRENCH.get());
                    output.accept(DEBUG_TOOL.get());
                    output.accept(MARKER.get());
                    output.accept(HAMMER.get());
                    output.accept(DISK.get());
                    output.accept(TYPE_BASE.get());
                    output.accept(STRUCTURAL_BASE.get());
                    CODE_BLOCKS.values().forEach(item -> output.accept(item.get()));
                    FLOOR.forEach(item -> output.accept(item.get()));
                    output.accept(DATA_ITEM.get());
                    TYPE_ITEMS.values().forEach(item -> output.accept(item.get()));
                    output.accept(VARIABLE_ITEM.get());
                    output.accept(CONSTANT_ITEM.get());
                    output.accept(LITERAL_ITEM.get());
                })
                .build());
    }

    private static Supplier<Item> register(String name, Supplier<Item> factory) {
        return Platform.INSTANCE.register(BuiltInRegistries.ITEM, name, factory);
    }
}
