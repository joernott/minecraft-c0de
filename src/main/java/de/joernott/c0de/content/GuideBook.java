package de.joernott.c0de.content;

import de.joernott.c0de.C0de;
import de.joernott.c0de.platform.Platform;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * The guide book of the mod (docs/guide), if Patchouli is installed: every player gets it once, when they join a
 * world for the first time with the mod. The book is also crafted from a book and a PROGRAM block (recipe
 * guide_book, which is only loaded with Patchouli). The mod doesn't depend on Patchouli: its item and data
 * component are looked up by their IDs.
 */
public final class GuideBook {
    /** The tag of the players who got the book. */
    private static final String GIVEN = "c0de.guide_given";

    private GuideBook() {}

    /** Registers the gift on joining. Called once when the mod is initialized. */
    public static void register() {
        Platform.INSTANCE.onPlayerJoin(GuideBook::joined);
    }

    private static void joined(ServerPlayer player) {
        if (player.getTags().contains(GIVEN) || !Platform.INSTANCE.isModLoaded("patchouli")) {
            return;
        }
        ItemStack book = book();
        if (book.isEmpty()) {
            return;
        }
        if (!player.getInventory().add(book)) {
            player.drop(book, false);
        }
        player.addTag(GIVEN);
    }

    /** The guide book, or an empty stack if Patchouli's book item or component doesn't exist. */
    @SuppressWarnings("unchecked")
    private static ItemStack book() {
        Item item = BuiltInRegistries.ITEM
                .getOptional(ResourceLocation.fromNamespaceAndPath("patchouli", "guide_book"))
                .orElse(Items.AIR);
        DataComponentType<?> component = BuiltInRegistries.DATA_COMPONENT_TYPE
                .getOptional(ResourceLocation.fromNamespaceAndPath("patchouli", "book"))
                .orElse(null);
        if (item == Items.AIR || component == null) {
            return ItemStack.EMPTY;
        }
        ItemStack book = new ItemStack(item);
        book.set((DataComponentType<ResourceLocation>) component, C0de.id("guide"));
        return book;
    }
}
