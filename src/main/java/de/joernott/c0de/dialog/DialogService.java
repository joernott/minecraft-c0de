package de.joernott.c0de.dialog;

import de.joernott.c0de.C0de;
import de.joernott.c0de.blockentity.PropertiesBlockEntity;
import de.joernott.c0de.core.edit.ItemRules;
import de.joernott.c0de.item.ValueItem;
import de.joernott.c0de.library.ModLibraries;
import de.joernott.c0de.network.OpenDialogPayload;
import de.joernott.c0de.network.SaveItemPayload;
import de.joernott.c0de.network.SavePropertiesPayload;
import de.joernott.c0de.platform.Platform;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** The server side of the dialogs of blocks with properties. */
public final class DialogService {
    /** Extra distance to the interaction range of the player, for latency and the time the dialog is open. */
    private static final double REACH_TOLERANCE = 2.0;

    private DialogService() {}

    /** Registers the payloads of the dialogs. Called once when the mod is initialized. */
    public static void register() {
        Platform.INSTANCE.registerClientbound(OpenDialogPayload.TYPE, OpenDialogPayload.CODEC);
        Platform.INSTANCE.registerServerbound(
                SavePropertiesPayload.TYPE,
                SavePropertiesPayload.CODEC,
                (payload, player) -> save(player, payload.pos(), payload).ifPresent(problem -> player.displayClientMessage(
                        Component.translatable("message.c0de.not_saved", problem), true)));
        Platform.INSTANCE.registerServerbound(
                SaveItemPayload.TYPE,
                SaveItemPayload.CODEC,
                (payload, player) -> saveItem(player, payload.properties()).ifPresent(problem -> player.displayClientMessage(
                        Component.translatable("message.c0de.not_saved", problem), true)));
    }

    /** Opens the dialog of the variable or constant item in the main hand of a player. */
    public static void openItem(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof ValueItem item)) {
            return;
        }
        ValueItem.Contents contents = ValueItem.Contents.of(stack);
        CompoundTag data = new CompoundTag();
        data.putString("name", contents.name());
        data.putString("type", contents.type());
        data.putString("value", ItemRules.display(contents.type(), contents.value()));
        data.putBoolean("constant", item.isConstant());
        List<String> structTypes = new ArrayList<>();
        ModLibraries.registry()
                .all()
                .forEach(library -> library.types().keySet().forEach(type -> structTypes.add(library.name() + "/" + type)));
        ListTag types = new ListTag();
        ItemRules.typeChoices(contents.type().split(" ")[0], structTypes)
                .forEach(type -> types.add(StringTag.valueOf(type)));
        data.put("types", types);
        Platform.INSTANCE.sendToPlayer(
                player, new OpenDialogPayload(player.blockPosition(), "value_item", item.isReadOnly(stack), data));
    }

    /** Changes the properties of the variable or constant item in the main hand of a player. */
    public static Optional<String> saveItem(ServerPlayer player, CompoundTag properties) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof ValueItem item)) {
            return Optional.of("There is no variable or constant item in the main hand");
        }
        return item.apply(
                stack, properties.getString("name"), properties.getString("type"), properties.getString("value"));
    }

    /** Opens the dialog of the block at the position for a player. */
    public static void open(ServerPlayer player, BlockPos pos) {
        OpenDialogPayload payload = payload(player, pos);
        if (payload != null) {
            Platform.INSTANCE.sendToPlayer(player, payload);
        }
    }

    /** The payload that opens the dialog, or null if the block has no dialog. */
    public static OpenDialogPayload payload(ServerPlayer player, BlockPos pos) {
        ServerLevel level = player.serverLevel();
        if (!(level.getBlockEntity(pos) instanceof PropertiesBlockEntity entity)) {
            return null;
        }
        return new OpenDialogPayload(
                pos, entity.dialog(), SheetProtection.isProtected(level, pos), entity.dialogData(level));
    }

    /**
     * Changes the properties of a block as entered in its dialog.
     *
     * @return the reason if the properties were not changed
     */
    public static Optional<String> save(ServerPlayer player, BlockPos pos, SavePropertiesPayload payload) {
        ServerLevel level = player.serverLevel();
        if (!level.isLoaded(pos) || !player.canInteractWithBlock(pos, REACH_TOLERANCE)) {
            return Optional.of("The block is too far away");
        }
        if (SheetProtection.isProtected(level, pos)) {
            return Optional.of("A process of the program is running");
        }
        if (!(level.getBlockEntity(pos) instanceof PropertiesBlockEntity entity)) {
            return Optional.of("The block has no properties");
        }
        Optional<String> problem = entity.apply(payload.properties(), level);
        problem.ifPresent(reason -> C0de.LOGGER.debug("Properties of {} not changed: {}", pos, reason));
        return problem;
    }
}
