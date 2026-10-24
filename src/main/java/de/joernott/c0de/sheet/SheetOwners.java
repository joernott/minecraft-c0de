package de.joernott.c0de.sheet;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * The owners of the sheets (docs/concept/01-dimension.md): the player who changed an empty sheet first. Only the
 * owner and administrators (permission level 2) can change a sheet, so that nobody can change the program behind
 * the trigger block of an administrator. A sheet whose first line becomes empty holds no program and has no owner
 * any more. Stored with the dimension ({@code dimensions/c0de/c0de/data/c0de_sheet_owners.dat}).
 */
public class SheetOwners extends SavedData {
    /** The permission level of administrators, like for the restricted functions of c0de/minecraft. */
    public static final int ADMIN_LEVEL = 2;
    private static final String NAME = "c0de_sheet_owners";

    // Vanilla (Fabric) can't load saved data without a data fixer type; NeoForge allows null
    private static final DataFixTypes DATA_FIX_TYPE =
        /*? if fabric {*/DataFixTypes.SAVED_DATA_COMMAND_STORAGE;
        /*?} else *///null;

    private static final SavedData.Factory<SheetOwners> FACTORY =
            new SavedData.Factory<>(SheetOwners::new, SheetOwners::load, DATA_FIX_TYPE);

    /** An owner: the UUID, and the name for messages. */
    public record Owner(UUID id, String name) {}

    private final Map<Integer, Owner> owners = new TreeMap<>();

    /** The owners of the sheets of the c0de dimension. */
    public static SheetOwners get(ServerLevel c0de) {
        return c0de.getDataStorage().computeIfAbsent(FACTORY, NAME);
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
        if (owner != null && owner.id().equals(player.getUUID())) {
            return Optional.empty();
        }
        if (player.hasPermissions(ADMIN_LEVEL)) {
            return Optional.empty();
        }
        if (owner == null) {
            if (claim) {
                owners.put(sheet, new Owner(player.getUUID(), player.getGameProfile().getName()));
                setDirty();
            }
            return Optional.empty();
        }
        return Optional.of(Component.translatable("message.c0de.sheet_owner", owner.name()));
    }

    /** The sheet is empty: it has no owner any more. */
    public void release(int sheet) {
        if (owners.remove(sheet) != null) {
            setDirty();
        }
    }

    private static SheetOwners load(CompoundTag tag, HolderLookup.Provider registries) {
        SheetOwners result = new SheetOwners();
        for (Tag element : tag.getList("owners", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) element;
            result.owners.put(entry.getInt("sheet"), new Owner(entry.getUUID("id"), entry.getString("name")));
        }
        return result;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        owners.forEach((sheet, owner) -> {
            CompoundTag entry = new CompoundTag();
            entry.putInt("sheet", sheet);
            entry.putUUID("id", owner.id());
            entry.putString("name", owner.name());
            list.add(entry);
        });
        tag.put("owners", list);
        return tag;
    }
}
