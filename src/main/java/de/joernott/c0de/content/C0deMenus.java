package de.joernott.c0de.content;

import de.joernott.c0de.io.DisplayMenu;
import de.joernott.c0de.io.IoMenu;
import de.joernott.c0de.menu.TriggerMenu;
import de.joernott.c0de.platform.Platform;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;

/** The menu types of the mod. */
public final class C0deMenus {
    public static Supplier<MenuType<TriggerMenu>> TRIGGER;
    public static Supplier<MenuType<IoMenu>> IO;
    public static Supplier<MenuType<DisplayMenu>> DISPLAY;
    public static Supplier<MenuType<de.joernott.c0de.disk.DiskDriveMenu>> DISK_DRIVE;

    private C0deMenus() {}

    public static void register() {
        TRIGGER = Platform.INSTANCE.register(
                BuiltInRegistries.MENU, "trigger", () -> Platform.INSTANCE.menuType(TriggerMenu::new));
        IO = Platform.INSTANCE.register(BuiltInRegistries.MENU, "io", () -> Platform.INSTANCE.menuType(IoMenu::new));
        DISPLAY = Platform.INSTANCE.register(
                BuiltInRegistries.MENU, "display", () -> Platform.INSTANCE.menuType(DisplayMenu::new));
        DISK_DRIVE = Platform.INSTANCE.register(
                BuiltInRegistries.MENU,
                "disk_drive",
                () -> Platform.INSTANCE.menuType(de.joernott.c0de.disk.DiskDriveMenu::new));
    }
}
