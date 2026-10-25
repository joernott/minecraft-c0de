package de.joernott.c0de.compat;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

/**
 * Saved data of the mod that is stored as a {@link CompoundTag} on all Minecraft versions (task 12.1). Since 1.21.5,
 * saved data is described by a {@code SavedDataType} with a codec instead of a {@code SavedData.Factory}; {@link
 * Kind} hides the difference. The file keeps its format: the compound of {@link #save} under the key {@code data}.
 */
public abstract class C0deSavedData extends SavedData {
    /** Writes the data into the tag and returns it. */
    public abstract CompoundTag save(CompoundTag tag, HolderLookup.Provider registries);

    // Vanilla (Fabric) can't load saved data without a data fixer type; NeoForge 1.21.1 allows null
    private static final DataFixTypes DATA_FIX_TYPE =
        /*? if fabric || >=1.21.5 {*/DataFixTypes.SAVED_DATA_COMMAND_STORAGE;
        /*?} else *///null;

    /**
     * A kind of saved data: its file name, how new data is created and how stored data is read.
     *
     * @param <T> the class of the data
     */
    public static final class Kind<T extends C0deSavedData> {
        //? if >=1.21.5 {
        /*private final net.minecraft.world.level.saveddata.SavedDataType<T> type;
        *///?} else {
        private final String name;
        private final SavedData.Factory<T> factory;
        //?}

        public Kind(String name, Supplier<T> constructor, BiFunction<CompoundTag, HolderLookup.Provider, T> load) {
            //? if >=1.21.5 {
            /*// The data of the mod doesn't refer to registries, so the empty registries suffice for reading it
            com.mojang.serialization.Codec<T> codec = CompoundTag.CODEC.xmap(
                    tag -> load.apply(tag, net.minecraft.core.RegistryAccess.EMPTY),
                    data -> data.save(new CompoundTag(), net.minecraft.core.RegistryAccess.EMPTY));
            //? if >=26.1 {
            /^// The files of the older versions (data/<name>.dat) are moved to data/minecraft/<name>.dat
            type = new net.minecraft.world.level.saveddata.SavedDataType<>(
                    ResourceLocation.withDefaultNamespace(name), constructor, codec, DATA_FIX_TYPE);
            ^///?} else {
            type = new net.minecraft.world.level.saveddata.SavedDataType<>(name, constructor, codec, DATA_FIX_TYPE);
            //?}
            *///?} else {
            this.name = name;
            factory = new SavedData.Factory<>(constructor, load::apply, DATA_FIX_TYPE);
            //?}
        }

        /** The data in a storage, created if it doesn't exist yet. */
        public T get(DimensionDataStorage storage) {
            //? if >=1.21.5 {
            /*return storage.computeIfAbsent(type);
            *///?} else {
            return storage.computeIfAbsent(factory, name);
            //?}
        }
    }
}
