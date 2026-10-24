package de.joernott.c0de.dialog;

import de.joernott.c0de.C0de;
import de.joernott.c0de.blockentity.PropertiesBlockEntity;
import de.joernott.c0de.core.edit.ItemRules;
import de.joernott.c0de.core.validation.Validation;
import de.joernott.c0de.dimension.C0deDimension;
import de.joernott.c0de.item.LiteralItem;
import de.joernott.c0de.item.ValueItem;
import de.joernott.c0de.library.ModLibraries;
import de.joernott.c0de.network.OpenDialogPayload;
import de.joernott.c0de.network.SaveItemPayload;
import de.joernott.c0de.network.SavePropertiesPayload;
import de.joernott.c0de.platform.Platform;
import de.joernott.c0de.sheet.ProgramRegistry;
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

    /** The programs of the c0de dimension with their functions, for the dialogs of devices. */
    public static CompoundTag programs(ServerPlayer player) {
        CompoundTag programs = new CompoundTag();
        ServerLevel c0de = player.server.getLevel(C0deDimension.LEVEL);
        if (c0de != null) {
            ProgramRegistry registry = ProgramRegistry.get(c0de);
            for (String name : registry.programs()) {
                ListTag functions = new ListTag();
                for (Validation.Function function : registry.functions(name)) {
                    functions.add(StringTag.valueOf(function.name()));
                }
                programs.put(name, functions);
            }
        }
        return programs;
    }

    /** Opens the dialog of the variable or constant item in the main hand of a player. */
    public static void openItem(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        CompoundTag data = new CompoundTag();
        String type;
        boolean readOnly = false;
        if (stack.getItem() instanceof ValueItem item) {
            ValueItem.Contents contents = ValueItem.Contents.of(stack);
            type = contents.type();
            data.putString("name", contents.name());
            data.putString("value", ItemRules.display(contents.type(), contents.value()));
            data.putBoolean("constant", item.isConstant());
            readOnly = item.isReadOnly(stack);
        } else if (stack.getItem() instanceof LiteralItem) {
            // A literal item has a type and a value, but no name; its type can always be changed
            LiteralItem.Contents contents = LiteralItem.Contents.of(stack);
            type = "";
            data.putString("name", "");
            data.putString("value", ItemRules.display(contents.type(), contents.value()));
            data.putBoolean("literal", true);
            data.putString("type", contents.type());
        } else {
            return;
        }
        if (!data.contains("type")) {
            data.putString("type", type);
        }
        List<String> structTypes = new ArrayList<>();
        ModLibraries.registry()
                .all()
                .forEach(library -> library.types().keySet().forEach(name -> structTypes.add(library.name() + "/" + name)));
        ListTag types = new ListTag();
        ItemRules.typeChoices(type.split(" ")[0], structTypes).forEach(choice -> types.add(StringTag.valueOf(choice)));
        data.put("types", types);
        Platform.INSTANCE.sendToPlayer(
                player, new OpenDialogPayload(player.blockPosition(), "value_item", readOnly, data));
    }

    /** Changes the properties of the variable or constant item in the main hand of a player. */
    public static Optional<String> saveItem(ServerPlayer player, CompoundTag properties) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof LiteralItem literal) {
            return literal.apply(stack, properties.getString("type"), properties.getString("value"));
        }
        if (!(stack.getItem() instanceof ValueItem item)) {
            return Optional.of("There is no variable, constant or literal item in the main hand");
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
                pos,
                entity.dialog(),
                SheetProtection.reason(player, level, pos, false).isPresent(),
                entity.dialogData(level));
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
        Optional<Component> protection = SheetProtection.reason(player, level, pos, true);
        if (protection.isPresent()) {
            return Optional.of(protection.get().getString());
        }
        if (!(level.getBlockEntity(pos) instanceof PropertiesBlockEntity entity)) {
            return Optional.of("The block has no properties");
        }
        Optional<String> problem = entity.apply(payload.properties(), level);
        problem.ifPresent(reason -> C0de.LOGGER.debug("Properties of {} not changed: {}", pos, reason));
        return problem;
    }
}
