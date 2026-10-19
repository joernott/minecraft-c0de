package de.joernott.c0de.content;

import de.joernott.c0de.item.WrenchItem;
import de.joernott.c0de.platform.Platform;
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
    public static Supplier<CreativeModeTab> TAB;

    private C0deItems() {}

    public static void register() {
        TRIGGER_BLOCK = register(
                "trigger_block", () -> new BlockItem(C0deBlocks.TRIGGER_BLOCK.get(), new Item.Properties()));
        WRENCH = register("wrench", () -> new WrenchItem(new Item.Properties().stacksTo(1)));
        TAB = Platform.INSTANCE.register(BuiltInRegistries.CREATIVE_MODE_TAB, "c0de", () -> CreativeModeTab.builder(
                        CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup.c0de"))
                .icon(() -> new ItemStack(WRENCH.get()))
                .displayItems((parameters, output) -> {
                    output.accept(TRIGGER_BLOCK.get());
                    output.accept(WRENCH.get());
                })
                .build());
    }

    private static Supplier<Item> register(String name, Supplier<Item> factory) {
        return Platform.INSTANCE.register(BuiltInRegistries.ITEM, name, factory);
    }
}
