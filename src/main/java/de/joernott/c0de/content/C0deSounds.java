package de.joernott.c0de.content;

import de.joernott.c0de.C0de;
import de.joernott.c0de.platform.Platform;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

/** The sounds of the mod (assets/c0de/sounds.json). */
public final class C0deSounds {
    /** A disk drive reads or writes a disk: seek clicks and the spinning disk (task 13.3). */
    public static Supplier<SoundEvent> DISK_DRIVE;

    private C0deSounds() {}

    public static void register() {
        DISK_DRIVE = Platform.INSTANCE.register(
                BuiltInRegistries.SOUND_EVENT,
                "disk_drive",
                () -> SoundEvent.createVariableRangeEvent(C0de.id("disk_drive")));
    }
}
