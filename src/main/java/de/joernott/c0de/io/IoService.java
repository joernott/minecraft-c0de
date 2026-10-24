package de.joernott.c0de.io;

import de.joernott.c0de.core.io.Console;
import de.joernott.c0de.core.io.TextScreen;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

/**
 * The devices of the processes (docs/concept/19-input-output.md): the display walls, I/O blocks, laptops and
 * tablets bound to a program and function. A process works with them through the {@link Console} of its program
 * and entry function.
 */
public final class IoService {
    private IoService() {}

    /** The console of the devices bound to a program and function. */
    public static Console console(MinecraftServer server, String program, String function) {
        return new DeviceConsole(server, program, function);
    }

    /** A screen of a device and the call that saves it and sends it to the players. */
    private record Screen(TextScreen screen, Runnable changed) {}

    /**
     * The devices bound to a program and function, looked up for every call, so that devices bound while a
     * process runs take part at once. Devices in unloaded chunks are left out.
     */
    private record DeviceConsole(MinecraftServer server, String program, String function) implements Console {
        private List<Screen> screens() {
            List<Screen> screens = new ArrayList<>();
            for (IoDevices.Device device : IoDevices.get(server).devices(program, function)) {
                ServerLevel level = server.getLevel(device.level());
                if (level == null || !level.isLoaded(device.pos())) {
                    continue;
                }
                if (device.kind() == IoDevices.Kind.DISPLAY
                        && level.getBlockEntity(device.pos()) instanceof DisplayBlockEntity display
                        && display.isOrigin()
                        && display.screen() != null) {
                    screens.add(new Screen(display.screen(), display::screenChanged));
                }
            }
            return screens;
        }

        private void all(Consumer<TextScreen> action) {
            for (Screen screen : screens()) {
                action.accept(screen.screen());
                screen.changed().run();
            }
        }

        @Override
        public void print(String text) {
            all(screen -> screen.println(text));
        }

        @Override
        public void cls() {
            all(TextScreen::clear);
        }

        @Override
        public void locate(int row, int column) {
            all(screen -> screen.locate(row, column));
        }

        @Override
        public void color(int foreground, int background) {
            all(screen -> screen.color(foreground, background));
        }

        @Override
        public int row() {
            List<Screen> screens = screens();
            return screens.isEmpty() ? 0 : screens.get(0).screen().row();
        }

        @Override
        public int column() {
            List<Screen> screens = screens();
            return screens.isEmpty() ? 0 : screens.get(0).screen().column();
        }
    }
}
