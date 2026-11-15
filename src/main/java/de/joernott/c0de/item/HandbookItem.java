package de.joernott.c0de.item;

import de.joernott.c0de.compat.Nbt;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
//? if <1.21.2 {
import net.minecraft.world.InteractionResultHolder;
//?}
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * A handbook (docs/concept/21-handbooks.md): a red binder; right-clicking with it opens the book on the client.
 * The seven binders of c0de have their book; the generic binder {@code c0de:handbook} serves the books of other
 * mods and finds its book and the colour of its label in its custom data ({@code book}, {@code color}), which the
 * recipes of the other mods set.
 */
public class HandbookItem extends Item {
    /** Opens a book on the client; set by the client of the mod, so that the server never loads the screen. */
    public static Consumer<String> opener = book -> {};

    /** The books of other mods the client knows, for the creative tab; set by the client of the mod. */
    public static Supplier<List<Known>> knownBooks = List::of;

    /** A book of another mod: its ID ({@code namespace:book}) and the colour of its label. */
    public record Known(String id, int color) {}

    private static final String BOOK = "book";
    private static final String COLOR = "color";
    private static final int WHITE = 0xF2F2F2;

    private final @Nullable String book;

    /** A binder of a book of c0de, e.g. {@code guide}; null for the generic binder. */
    public HandbookItem(Properties properties, @Nullable String book) {
        super(properties);
        this.book = book;
    }

    /** The book of a binder, e.g. {@code guide} or {@code c0de_net:net}, or null if it has none. */
    public @Nullable String book(ItemStack stack) {
        if (book != null) {
            return book;
        }
        String id = Nbt.string(customData(stack), BOOK);
        return id.isEmpty() ? null : id;
    }

    /** The colour of the label of a generic binder (white if it has none). */
    public static int color(ItemStack stack) {
        CompoundTag tag = customData(stack);
        return tag.contains(COLOR) ? Nbt.integer(tag, COLOR) & 0xFFFFFF : WHITE;
    }

    /** The generic binder of a book of another mod, with the colour of its label. */
    public static ItemStack stack(Item handbook, String id, int color) {
        ItemStack stack = new ItemStack(handbook);
        CompoundTag tag = new CompoundTag();
        tag.putString(BOOK, id);
        tag.putInt(COLOR, color);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        //? if >=1.21.4 {
        /*// The item model reads the colour of the label from the first colour of the custom model data
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new net.minecraft.world.item.component.CustomModelData(
                List.of(), List.of(), List.of(), List.of(color)));
        *///?}
        return stack;
    }

    private static CompoundTag customData(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    /** A generic binder is named after its book: the translation {@code book.<namespace>.<book>.name}. */
    @Override
    public Component getName(ItemStack stack) {
        String id = book == null ? book(stack) : null;
        if (id == null || !id.contains(":")) {
            return super.getName(stack);
        }
        return Component.translatable("book." + id.replace(':', '.') + ".name");
    }

    //? if >=1.21.2 {
    /*@Override
    public net.minecraft.world.InteractionResult use(Level level, Player player, InteractionHand hand) {
        String id = book(player.getItemInHand(hand));
        if (id == null) {
            return net.minecraft.world.InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            opener.accept(id);
        }
        return net.minecraft.world.InteractionResult.SUCCESS;
    }
    *///?} else {
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        String id = book(stack);
        if (id == null) {
            return InteractionResultHolder.pass(stack);
        }
        if (level.isClientSide()) {
            opener.accept(id);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
    //?}
}
