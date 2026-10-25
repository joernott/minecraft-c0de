package de.joernott.c0de.sheet;

import de.joernott.c0de.compat.C0deSavedData;
import de.joernott.c0de.compat.Compat;
import de.joernott.c0de.compat.Nbt;
import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.dimension.C0deDimension;
import de.joernott.c0de.network.GiveSheetPayload;
import de.joernott.c0de.network.OpenDialogPayload;
import de.joernott.c0de.network.SheetOwnersPayload;
import de.joernott.c0de.platform.Platform;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * The owners of the sheets (docs/concept/01-dimension.md): the player who changed an empty sheet first. Only the
 * owner and administrators (permission level 2) can change a sheet, so that nobody can change the program behind
 * the trigger block of an administrator. A sheet whose first line becomes empty holds no program and has no owner
 * any more. Stored with the dimension ({@code dimensions/c0de/c0de/data/c0de_sheet_owners.dat}).
 */
public class SheetOwners extends C0deSavedData {
    /** The permission level of administrators, like for the restricted functions of c0de/minecraft. */
    public static final int ADMIN_LEVEL = 2;
    private static final String NAME = "c0de_sheet_owners";

    private static final C0deSavedData.Kind<SheetOwners> KIND =
            new C0deSavedData.Kind<>(NAME, SheetOwners::new, SheetOwners::load);

    /** An owner: the UUID, and the name for messages. */
    public record Owner(UUID id, String name) {}

    private final Map<Integer, Owner> owners = new TreeMap<>();

    /** The owners of the sheets of the c0de dimension. */
    public static SheetOwners get(ServerLevel c0de) {
        return KIND.get(c0de.getDataStorage());
    }

    public Optional<Owner> owner(int sheet) {
        return Optional.ofNullable(owners.get(sheet));
    }

    /**
     * Checks whether a player may change a sheet.
     *
     * @param claim true if the player changes the sheet now: an unowned sheet then becomes theirs
     * @return the message for the player if not
     */
    public Optional<Component> check(ServerPlayer player, int sheet, boolean claim) {
        Owner owner = owners.get(sheet);
        if (owner == null) {
            // Administrators become owners too, so that their sheets are protected like everybody's
            if (claim) {
                owners.put(sheet, new Owner(player.getUUID(), Compat.name(player.getGameProfile())));
                changed(Compat.server(player));
            }
            return Optional.empty();
        }
        if (owner.id().equals(player.getUUID()) || Compat.isAdministrator(player)) {
            return Optional.empty();
        }
        return Optional.of(Component.translatable("message.c0de.sheet_owner", owner.name()));
    }

    /** The sheet is empty: it has no owner any more. */
    public void release(MinecraftServer server, int sheet) {
        if (owners.remove(sheet) != null) {
            changed(server);
        }
    }

    /**
     * Gives a sheet to another player (task 11.11.7): owners can give their own sheets, administrators every sheet.
     *
     * @return the reason if the sheet was not given
     */
    public Optional<String> give(ServerPlayer player, int sheet, ServerPlayer receiver) {
        Owner owner = owners.get(sheet);
        boolean own = owner != null && owner.id().equals(player.getUUID());
        if (!own && !Compat.isAdministrator(player)) {
            return Optional.of("Only the owner of the sheet and administrators can give it away");
        }
        owners.put(sheet, new Owner(receiver.getUUID(), Compat.name(receiver.getGameProfile())));
        changed(Compat.server(player));
        return Optional.empty();
    }

    // ------------------------------------------------------------------ clients

    /** The width of the floor that shows the information of a sheet, from its x=0. */
    public static final int INFO_WIDTH = 5;

    /**
     * True for the floor that shows the information of a sheet: the black floor north of its corner (z=-2 and z=-1,
     * x=0 to x=4 of the sheet).
     */
    public static boolean isInfo(Level level, BlockPos pos) {
        return level.dimension().equals(C0deDimension.LEVEL)
                && pos.getY() == SheetGeometry.FLOOR_Y
                && (pos.getZ() == -1 || pos.getZ() == -2)
                && pos.getX() >= 0
                && SheetGeometry.relativeX(pos.getX()) < INFO_WIDTH;
    }

    /** Opens the dialog that gives the sheet of the information to another player (wrench). */
    public static void openGive(ServerPlayer player, BlockPos pos) {
        int sheet = pos.getX() / SheetGeometry.SHEET_WIDTH;
        CompoundTag data = new CompoundTag();
        data.putInt("sheet", sheet);
        data.putString("owner", get(Compat.level(player)).owner(sheet).map(Owner::name).orElse(""));
        ListTag players = new ListTag();
        Compat.server(player).getPlayerList().getPlayers().stream()
                .map(online -> Compat.name(online.getGameProfile()))
                .sorted()
                .forEach(name -> players.add(StringTag.valueOf(name)));
        data.put("players", players);
        Platform.INSTANCE.sendToPlayer(player, new OpenDialogPayload(pos, "sheet_owner", false, data));
    }

    /** Sends the owners to every player who joins. Called once when the mod is initialized. */
    public static void register() {
        Platform.INSTANCE.registerClientbound(SheetOwnersPayload.TYPE, SheetOwnersPayload.CODEC);
        Platform.INSTANCE.registerServerbound(GiveSheetPayload.TYPE, GiveSheetPayload.CODEC, (payload, player) -> {
            ServerLevel c0de = Compat.server(player).getLevel(C0deDimension.LEVEL);
            ServerPlayer receiver = Compat.server(player).getPlayerList().getPlayerByName(payload.player());
            Optional<String> problem = c0de == null || receiver == null
                    ? Optional.of("The player " + payload.player() + " is not online")
                    : get(c0de).give(player, payload.sheet(), receiver);
            Compat.message(player, problem.map(reason -> Component.translatable("message.c0de.not_saved", reason))
                            .orElse(Component.translatable(
                                    "message.c0de.sheet_given", payload.sheet(), payload.player())), true);
        });
        Platform.INSTANCE.onPlayerJoin(player -> {
            ServerLevel c0de = Compat.server(player).getLevel(C0deDimension.LEVEL);
            if (c0de != null) {
                Platform.INSTANCE.sendToPlayer(player, get(c0de).payload());
            }
        });
    }

    private SheetOwnersPayload payload() {
        CompoundTag names = new CompoundTag();
        owners.forEach((sheet, owner) -> names.putString(Integer.toString(sheet), owner.name()));
        return new SheetOwnersPayload(names);
    }

    /** Saves the owners and sends them to all players. */
    private void changed(MinecraftServer server) {
        setDirty();
        SheetOwnersPayload payload = payload();
        server.getPlayerList().getPlayers().forEach(player -> Platform.INSTANCE.sendToPlayer(player, payload));
    }

    private static SheetOwners load(CompoundTag tag, HolderLookup.Provider registries) {
        SheetOwners result = new SheetOwners();
        for (Tag element : Nbt.list(tag, "owners", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) element;
            result.owners.put(Nbt.integer(entry, "sheet"), new Owner(Nbt.uuid(entry, "id"), Nbt.string(entry, "name")));
        }
        return result;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        owners.forEach((sheet, owner) -> {
            CompoundTag entry = new CompoundTag();
            entry.putInt("sheet", sheet);
            Nbt.putUuid(entry, "id", owner.id());
            entry.putString("name", owner.name());
            list.add(entry);
        });
        tag.put("owners", list);
        return tag;
    }
}
