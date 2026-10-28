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
    public static Supplier<Item> DISK_DRIVE;
    public static Supplier<Item> LITERAL_ITEM;
    public static Supplier<Item> TIMER_BLOCK;
    public static Supplier<Item> DEBUG_TRIGGER_BLOCK;
    public static Supplier<Item> WRENCH;
    public static Supplier<Item> DEBUG_TOOL;
    public static Supplier<Item> MARKER;
    public static Supplier<Item> HAMMER;
    public static Supplier<Item> PLIERS;
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
        DISPLAY_BLOCK = blockItem("display_block", C0deBlocks.DISPLAY_BLOCK);
        IO_BLOCK = blockItem("io_block", C0deBlocks.IO_BLOCK);
        LAPTOP = blockItem("laptop", C0deBlocks.LAPTOP);
        DISK_DRIVE = blockItem("disk_drive", C0deBlocks.DISK_DRIVE);
        TABLET = register("tablet", properties -> new de.joernott.c0de.item.TabletItem(properties.stacksTo(1)));
        TRIGGER_BLOCK = blockItem("trigger_block", C0deBlocks.TRIGGER_BLOCK);
        TIMER_BLOCK = blockItem("timer_block", C0deBlocks.TIMER_BLOCK);
        DEBUG_TRIGGER_BLOCK = blockItem("debug_trigger_block", C0deBlocks.DEBUG_TRIGGER_BLOCK);
        WRENCH = register("wrench", properties -> new WrenchItem(properties.stacksTo(1)));
        DEBUG_TOOL = register("debug_tool", properties -> new DebugToolItem(properties.stacksTo(1)));
        MARKER = register("marker", properties -> new ToolItem(properties.stacksTo(1)));
        HAMMER = register("hammer", properties -> new ToolItem(properties.stacksTo(1)));
        PLIERS = register("pliers", properties -> new ToolItem(properties.stacksTo(1)));
        DISK = register("c0de_disk", properties -> new DiskItem(properties.stacksTo(1)));
        // The floor can be broken in creative mode, so creative players need the blocks to repair it
        // The suppliers of the blocks can't be called before the blocks are registered, so the names are listed
        java.util.Map<String, Supplier<Block>> floor = new java.util.LinkedHashMap<>();
        floor.put("white_c0de_bedrock", C0deBlocks.WHITE_C0DE_BEDROCK);
        floor.put("light_green_c0de_bedrock", C0deBlocks.LIGHT_GREEN_C0DE_BEDROCK);
        floor.put("black_c0de_bedrock", C0deBlocks.BLACK_C0DE_BEDROCK);
        floor.put("cursor_c0de_bedrock", C0deBlocks.CURSOR_C0DE_BEDROCK);
        floor.forEach((name, block) -> FLOOR.add(blockItem(name, block)));
        DATA_ITEM = register("data_item", Item::new);
        for (String type : TYPES) {
            TYPE_ITEMS.put(type, register("type_" + type.toLowerCase(java.util.Locale.ROOT) + "_item", Item::new));
        }
        VARIABLE_ITEM = register("variable_item", properties -> new ValueItem(properties, false));
        CONSTANT_ITEM = register("constant_item", properties -> new ValueItem(properties, true));
        LITERAL_ITEM = register("literal_item", properties -> new de.joernott.c0de.item.LiteralItem(properties));
        TYPE_BASE = register("type_base", Item::new);
        STRUCTURAL_BASE = register("structural_base", Item::new);
        for (CodeBlockType type : CodeBlockType.values()) {
            CODE_BLOCKS.put(type, blockItem(type.id(), () -> C0deBlocks.get(type)));
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
                    output.accept(PLIERS.get());
                    output.accept(DISK.get());
                    output.accept(DISK_DRIVE.get());
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

    /** Registers an item; since 1.21.2 the properties contain the ID of the item. */
    private static Supplier<Item> register(String name, java.util.function.Function<Item.Properties, Item> factory) {
        Item.Properties properties = new Item.Properties();
        //? if >=1.21.2 {
        /*properties.setId(net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.ITEM, de.joernott.c0de.C0de.id(name)));
        *///?}
        return Platform.INSTANCE.register(BuiltInRegistries.ITEM, name, () -> factory.apply(properties));
    }

    /** Registers the item of a block, named like the block. */
    private static Supplier<Item> blockItem(String name, Supplier<Block> block) {
        //? if >=1.21.2 {
        /*return register(name, properties -> new BlockItem(block.get(), properties.useBlockDescriptionPrefix()));
        *///?} else {
        return register(name, properties -> new BlockItem(block.get(), properties));
        //?}
    }
}
