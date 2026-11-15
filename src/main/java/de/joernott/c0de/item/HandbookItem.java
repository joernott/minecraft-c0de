package de.joernott.c0de.item;

import java.util.function.Consumer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
//? if <1.21.2 {
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
//?}
import net.minecraft.world.level.Level;

/**
 * A handbook (docs/concept/21-handbooks.md): a red binder; right-clicking with it opens the book on the client.
 */
public class HandbookItem extends Item {
    /** Opens a book on the client; set by the client of the mod, so that the server never loads the screen. */
    public static Consumer<String> opener = book -> {};

    private final String book;

    public HandbookItem(Properties properties, String book) {
        super(properties);
        this.book = book;
    }

    /** The book of the item, e.g. {@code guide}. */
    public String book() {
        return book;
    }

    //? if >=1.21.2 {
    /*@Override
    public net.minecraft.world.InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            opener.accept(book);
        }
        return net.minecraft.world.InteractionResult.SUCCESS;
    }
    *///?} else {
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            opener.accept(book);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
    //?}
}
