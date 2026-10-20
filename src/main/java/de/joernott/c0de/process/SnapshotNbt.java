package de.joernott.c0de.process;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

/**
 * Converts the plain data of a cursor snapshot (maps, lists, strings, longs, doubles and booleans, see
 * {@link de.joernott.c0de.core.cursor.CursorSnapshot}) to NBT and back. List elements are wrapped in compounds,
 * because NBT lists can only hold elements of one type.
 */
public final class SnapshotNbt {
    private SnapshotNbt() {}

    public static Tag toNbt(Object value) {
        return switch (value) {
            case Map<?, ?> map -> {
                CompoundTag tag = new CompoundTag();
                map.forEach((key, element) -> tag.put(String.valueOf(key), toNbt(element)));
                yield tag;
            }
            case List<?> list -> {
                ListTag tag = new ListTag();
                for (Object element : list) {
                    CompoundTag wrapper = new CompoundTag();
                    wrapper.put("v", toNbt(element));
                    tag.add(wrapper);
                }
                yield tag;
            }
            case String string -> StringTag.valueOf(string);
            case Boolean bool -> ByteTag.valueOf(bool);
            case Long number -> LongTag.valueOf(number);
            case Integer number -> LongTag.valueOf(number);
            case Double number -> DoubleTag.valueOf(number);
            default -> throw new IllegalArgumentException("Can't store " + value.getClass().getSimpleName());
        };
    }

    public static Object fromNbt(Tag tag) {
        return switch (tag) {
            case CompoundTag compound -> {
                Map<String, Object> map = new LinkedHashMap<>();
                for (String key : compound.getAllKeys()) {
                    map.put(key, fromNbt(compound.get(key)));
                }
                yield map;
            }
            case ListTag list -> {
                List<Object> result = new ArrayList<>();
                for (Tag element : list) {
                    result.add(fromNbt(((CompoundTag) element).get("v")));
                }
                yield result;
            }
            case StringTag string -> string.getAsString();
            case ByteTag bool -> bool.getAsByte() != 0;
            case LongTag number -> number.getAsLong();
            case DoubleTag number -> number.getAsDouble();
            default -> throw new IllegalArgumentException("Unexpected tag " + tag);
        };
    }
}
