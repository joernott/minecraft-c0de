package de.joernott.c0de.compat;

import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

/**
 * Reading and writing NBT the same way on all Minecraft versions (task 12.1). Since 1.21.5, the getters of
 * {@link CompoundTag} return {@code Optional}; these helpers return the defaults of the older getters instead
 * (empty string, 0, false, an empty compound or list), so the code of the mod stays the same. UUIDs are stored as
 * arrays of four integers on all versions.
 */
public final class Nbt {
    private Nbt() {}

    public static String string(CompoundTag tag, String key) {
        //? if >=1.21.5 {
        /*return tag.getStringOr(key, "");
        *///?} else {
        return tag.getString(key);
        //?}
    }

    public static int integer(CompoundTag tag, String key) {
        //? if >=1.21.5 {
        /*return tag.getIntOr(key, 0);
        *///?} else {
        return tag.getInt(key);
        //?}
    }

    public static long longValue(CompoundTag tag, String key) {
        //? if >=1.21.5 {
        /*return tag.getLongOr(key, 0);
        *///?} else {
        return tag.getLong(key);
        //?}
    }

    public static double doubleValue(CompoundTag tag, String key) {
        //? if >=1.21.5 {
        /*return tag.getDoubleOr(key, 0);
        *///?} else {
        return tag.getDouble(key);
        //?}
    }

    public static float floatValue(CompoundTag tag, String key) {
        //? if >=1.21.5 {
        /*return tag.getFloatOr(key, 0);
        *///?} else {
        return tag.getFloat(key);
        //?}
    }

    public static boolean bool(CompoundTag tag, String key) {
        //? if >=1.21.5 {
        /*return tag.getBooleanOr(key, false);
        *///?} else {
        return tag.getBoolean(key);
        //?}
    }

    public static byte[] byteArray(CompoundTag tag, String key) {
        //? if >=1.21.5 {
        /*return tag.getByteArray(key).orElse(new byte[0]);
        *///?} else {
        return tag.getByteArray(key);
        //?}
    }

    public static int[] intArray(CompoundTag tag, String key) {
        //? if >=1.21.5 {
        /*return tag.getIntArray(key).orElse(new int[0]);
        *///?} else {
        return tag.getIntArray(key);
        //?}
    }

    /** The compound under the key, or an empty one. */
    public static CompoundTag compound(CompoundTag tag, String key) {
        //? if >=1.21.5 {
        /*return tag.getCompoundOrEmpty(key);
        *///?} else {
        return tag.getCompound(key);
        //?}
    }

    /** The list under the key, or an empty one; the type of the elements is only checked by older versions. */
    public static ListTag list(CompoundTag tag, String key, int type) {
        //? if >=1.21.5 {
        /*return tag.getListOrEmpty(key);
        *///?} else {
        return tag.getList(key, type);
        //?}
    }

    /** The compound at an index of a list, or an empty one. */
    public static CompoundTag compound(ListTag list, int index) {
        //? if >=1.21.5 {
        /*return list.getCompoundOrEmpty(index);
        *///?} else {
        return list.getCompound(index);
        //?}
    }

    /** The text of a tag: the value of a string tag, the text form of other tags. */
    public static String text(Tag tag) {
        //? if >=1.21.5 {
        /*return tag.asString().orElse(tag.toString());
        *///?} else {
        return tag.getAsString();
        //?}
    }

    public static Set<String> keys(CompoundTag tag) {
        //? if >=1.21.5 {
        /*return tag.keySet();
        *///?} else {
        return tag.getAllKeys();
        //?}
    }

    public static boolean hasUuid(CompoundTag tag, String key) {
        //? if >=1.21.5 {
        /*return tag.read(key, net.minecraft.core.UUIDUtil.CODEC).isPresent();
        *///?} else {
        return tag.hasUUID(key);
        //?}
    }

    /** The UUID under the key, or null. */
    public static UUID uuid(CompoundTag tag, String key) {
        //? if >=1.21.5 {
        /*return tag.read(key, net.minecraft.core.UUIDUtil.CODEC).orElse(null);
        *///?} else {
        return tag.hasUUID(key) ? tag.getUUID(key) : null;
        //?}
    }

    public static void putUuid(CompoundTag tag, String key, UUID uuid) {
        //? if >=1.21.5 {
        /*tag.store(key, net.minecraft.core.UUIDUtil.CODEC, uuid);
        *///?} else {
        tag.putUUID(key, uuid);
        //?}
    }
}
