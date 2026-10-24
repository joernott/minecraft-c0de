package de.joernott.c0de.io;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * The devices bound to programs and functions (docs/concept/19-input-output.md): display walls (by the position
 * of the block that holds their screen), I/O blocks and laptops, in the order they were bound. Stored with the
 * overworld ({@code data/c0de_devices.dat}), so that processes find the devices of unloaded chunks too.
 */
public class IoDevices extends SavedData {
    private static final String NAME = "c0de_devices";

    // Vanilla (Fabric) can't load saved data without a data fixer type; NeoForge allows null
    private static final DataFixTypes DATA_FIX_TYPE =
        /*? if fabric {*/DataFixTypes.SAVED_DATA_COMMAND_STORAGE;
        /*?} else *///null;

    private static final SavedData.Factory<IoDevices> FACTORY =
            new SavedData.Factory<>(IoDevices::new, IoDevices::load, DATA_FIX_TYPE);

    /** The kinds of devices. */
    public enum Kind {
        DISPLAY,
        IO,
        LAPTOP
    }

    /**
     * A bound device.
     *
     * @param order the order of binding: smaller values were bound earlier
     */
    public record Device(Kind kind, ResourceKey<Level> level, BlockPos pos, String program, String function, long order) {}

    private final List<Device> devices = new ArrayList<>();
    private long next;

    public static IoDevices get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, NAME);
    }

    /** Binds a device; a device that was bound before moves to the end of the order. */
    public void bind(Kind kind, ResourceKey<Level> level, BlockPos pos, String program, String function) {
        remove(level, pos);
        devices.add(new Device(kind, level, pos.immutable(), program, function, next++));
        setDirty();
    }

    /** Moves a device to another position and keeps its place in the order (the screen of a wall moved). */
    public void move(ResourceKey<Level> level, BlockPos from, BlockPos to) {
        for (int i = 0; i < devices.size(); i++) {
            Device device = devices.get(i);
            if (device.level().equals(level) && device.pos().equals(from)) {
                devices.set(i, new Device(device.kind(), level, to.immutable(), device.program(), device.function(),
                        device.order()));
                setDirty();
            }
        }
    }

    /** Removes the device at a position, e.g. because it was broken. */
    public void remove(ResourceKey<Level> level, BlockPos pos) {
        if (devices.removeIf(device -> device.level().equals(level) && device.pos().equals(pos))) {
            setDirty();
        }
    }

    /** The devices bound to a program and function, the earliest bound first. */
    public List<Device> devices(String program, String function) {
        return devices.stream()
                .filter(device -> device.program().equals(program) && device.function().equals(function))
                .sorted(Comparator.comparingLong(Device::order))
                .toList();
    }

    private static IoDevices load(CompoundTag tag, HolderLookup.Provider registries) {
        IoDevices result = new IoDevices();
        result.next = tag.getLong("next");
        for (Tag element : tag.getList("devices", Tag.TAG_COMPOUND)) {
            CompoundTag device = (CompoundTag) element;
            try {
                result.devices.add(new Device(
                        Kind.valueOf(device.getString("kind")),
                        ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(device.getString("level"))),
                        BlockPos.of(device.getLong("pos")),
                        device.getString("program"),
                        device.getString("function"),
                        device.getLong("order")));
            } catch (IllegalArgumentException e) {
                // A device of an unknown kind or dimension is left out
            }
        }
        return result;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLong("next", next);
        ListTag list = new ListTag();
        for (Device device : devices) {
            CompoundTag entry = new CompoundTag();
            entry.putString("kind", device.kind().name());
            entry.putString("level", device.level().location().toString());
            entry.putLong("pos", device.pos().asLong());
            entry.putString("program", device.program());
            entry.putString("function", device.function());
            entry.putLong("order", device.order());
            list.add(entry);
        }
        tag.put("devices", list);
        return tag;
    }
}
