package de.joernott.c0de.content;

import de.joernott.c0de.compat.Compat;
import de.joernott.c0de.platform.Platform;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * The guide of the mod (docs/concept/21-handbooks.md): every player gets the red binder of the guide once, when
 * they join a world with the mod. The tag is new in 0.6.0, so players who got the Patchouli guide before get the
 * binder too.
 */
public final class GuideBook {
    /** The tag of the players who got the guide. */
    private static final String GIVEN = "c0de.handbook_given";

    private GuideBook() {}

    /** Registers the gift on joining. Called once when the mod is initialized. */
    public static void register() {
        Platform.INSTANCE.onPlayerJoin(GuideBook::joined);
    }

    private static void joined(ServerPlayer player) {
        if (player.getTags().contains(GIVEN)) {
            return;
        }
        Compat.give(player, new ItemStack(C0deItems.HANDBOOK_ITEMS.get("guide").get()));
        player.addTag(GIVEN);
    }
}
