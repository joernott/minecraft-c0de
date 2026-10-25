package de.joernott.c0de.item;

import de.joernott.c0de.compat.Compat;
import de.joernott.c0de.compat.Nbt;
import de.joernott.c0de.io.Terminals;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * The tablet (docs/concept/19-input-output.md): a laptop as an item. Left-clicking with it in the main hand opens
 * its terminal. The item keeps the ID of its terminal; the binding, screen and history are kept in
 * {@link Terminals}.
 */
public class TabletItem extends Item {
    public TabletItem(Properties properties) {
        super(properties);
    }

    /** Opens the terminal of the tablet in the main hand of the player, if there is one. */
    public static void open(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof TabletItem) {
            Terminals terminals = Terminals.get(Compat.server(player));
            terminals.open(player, terminals.terminal(terminal(stack)));
        }
    }

    /** The ID of the terminal of a tablet; a new tablet gets one when it is first used. */
    public static UUID terminal(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!Nbt.hasUuid(tag, "terminal")) {
            Nbt.putUuid(tag, "terminal", UUID.randomUUID());
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        return Nbt.uuid(tag, "terminal");
    }
}
