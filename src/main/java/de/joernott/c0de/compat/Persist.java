package de.joernott.c0de.compat;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * Saving the data of block entities, entities and saved data as {@link CompoundTag}s on all Minecraft versions (task
 * 12.1). Since 1.21.6, Minecraft saves through {@code ValueOutput} and {@code ValueInput}; {@link #ALL} copies all
 * entries of a compound between them, so the mod keeps its compounds and worlds keep their format.
 */
public final class Persist {
    private Persist() {}

    /** A codec that reads and writes all entries of the compound it is applied to. */
    public static final MapCodec<CompoundTag> ALL = new MapCodec<>() {
        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.empty();
        }

        @Override
        public <T> DataResult<CompoundTag> decode(DynamicOps<T> ops, MapLike<T> input) {
            CompoundTag tag = new CompoundTag();
            input.entries().forEach(entry -> ops.getStringValue(entry.getFirst())
                    .result()
                    .ifPresent(key -> tag.put(key, ops.convertTo(NbtOps.INSTANCE, entry.getSecond()))));
            return DataResult.success(tag);
        }

        @Override
        public <T> RecordBuilder<T> encode(CompoundTag input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            for (String key : Nbt.keys(input)) {
                prefix.add(key, NbtOps.INSTANCE.convertTo(ops, input.get(key)));
            }
            return prefix;
        }
    };

    /** An item stack as a tag, as {@code ItemStack.save} of Minecraft 1.21.1 wrote it. */
    public static Tag saveItem(ItemStack stack, HolderLookup.Provider registries) {
        return ItemStack.CODEC
                .encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), stack)
                .result()
                .orElseGet(CompoundTag::new);
    }

    /** An item stack read from a tag written by {@link #saveItem}, if it is valid. */
    public static java.util.Optional<ItemStack> loadItem(Tag tag, HolderLookup.Provider registries) {
        return ItemStack.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag).result();
    }

    /** The items of a container, as {@code SimpleContainer.createTag} of Minecraft 1.21.1 wrote them. */
    public static ListTag saveItems(Container container, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                ItemStack.CODEC
                        .encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), stack)
                        .result()
                        .ifPresent(list::add);
            }
        }
        return list;
    }

    /** Puts the items of a list written by {@link #saveItems} into a container, from its first slot. */
    public static void loadItems(Container container, ListTag list, HolderLookup.Provider registries) {
        container.clearContent();
        int slot = 0;
        for (Tag element : list) {
            ItemStack stack = ItemStack.CODEC
                    .parse(registries.createSerializationContext(NbtOps.INSTANCE), element)
                    .result()
                    .orElse(ItemStack.EMPTY);
            if (!stack.isEmpty() && slot < container.getContainerSize()) {
                container.setItem(slot++, stack);
            }
        }
    }
}
