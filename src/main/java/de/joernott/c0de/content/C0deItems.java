package de.joernott.c0de.content;

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

/** The items of the mod and its creative mode tab. */
public final class C0deItems {
    public static Supplier<Item> TRIGGER_BLOCK;
    public static Supplier<Item> WRENCH;
    public static Supplier<Item> TYPE_BASE;
    public static Supplier<Item> STRUCTURAL_BASE;

    /** The items of the language blocks. */
    public static final Map<CodeBlockType, Supplier<Item>> CODE_BLOCKS = new EnumMap<>(CodeBlockType.class);
    public static Supplier<CreativeModeTab> TAB;

    private C0deItems() {}

    public static void register() {
        TRIGGER_BLOCK = register(
                "trigger_block", () -> new BlockItem(C0deBlocks.TRIGGER_BLOCK.get(), new Item.Properties()));
        WRENCH = register("wrench", () -> new WrenchItem(new Item.Properties().stacksTo(1)));
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
                    output.accept(WRENCH.get());
                    output.accept(TYPE_BASE.get());
                    output.accept(STRUCTURAL_BASE.get());
                    CODE_BLOCKS.values().forEach(item -> output.accept(item.get()));
                })
                .build());
    }

    private static Supplier<Item> register(String name, Supplier<Item> factory) {
        return Platform.INSTANCE.register(BuiltInRegistries.ITEM, name, factory);
    }
}
