package de.joernott.c0de.io;

import de.joernott.c0de.content.C0deItems;
import de.joernott.c0de.core.edit.ItemRules;
import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.io.Console;
import de.joernott.c0de.core.io.TextScreen;
import de.joernott.c0de.core.library.standard.JsonValues;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0de.dialog.DialogService;
import de.joernott.c0de.item.LiteralItem;
import de.joernott.c0de.item.ValueItem;
import de.joernott.c0de.network.OpenDevicePayload;
import de.joernott.c0de.platform.Platform;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

/**
 * The devices of the processes (docs/concept/19-input-output.md): the display walls, I/O blocks, laptops and
 * tablets bound to a program and function. A process works with them through the {@link Console} of its program
 * and entry function.
 */
public final class IoService {
    private static final double REACH_TOLERANCE = 2.0;

    private IoService() {}

    /** Registers the payloads of the devices. Called once when the mod is initialized. */
    public static void register() {
        Platform.INSTANCE.registerServerbound(OpenDevicePayload.TYPE, OpenDevicePayload.CODEC, (payload, player) -> {
            if (player.serverLevel().isLoaded(payload.pos())
                    && player.canInteractWithBlock(payload.pos(), REACH_TOLERANCE)) {
                DialogService.open(player, payload.pos());
            }
        });
    }

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

        /** The I/O blocks, the earliest bound first. */
        private List<IoBlockEntity> ioBlocks() {
            List<IoBlockEntity> blocks = new ArrayList<>();
            for (IoDevices.Device device : IoDevices.get(server).devices(program, function)) {
                ServerLevel level = server.getLevel(device.level());
                if (device.kind() == IoDevices.Kind.IO
                        && level != null
                        && level.isLoaded(device.pos())
                        && level.getBlockEntity(device.pos()) instanceof IoBlockEntity block) {
                    blocks.add(block);
                }
            }
            return blocks;
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

        /**
         * Every I/O block with an uninitialized variable item gets the value: the item leaves the input and
         * appears in the output with the name, type and value. A block whose output is full is skipped. Without
         * such an item anywhere, WRITE waits.
         */
        @Override
        public boolean write(String name, Value value) {
            boolean written = false;
            for (IoBlockEntity block : ioBlocks()) {
                SimpleContainer input = block.input();
                for (int i = 0; i < input.getContainerSize(); i++) {
                    ItemStack stack = input.getItem(i);
                    if (stack.getItem() instanceof ValueItem item
                            && !item.isConstant()
                            && ValueItem.Contents.of(stack).isEmpty()) {
                        ItemStack result = stack.copyWithCount(1);
                        new ValueItem.Contents(name, value.type().toString(), JsonValues.write(value))
                                .writeTo(result);
                        if (!block.output().canAddItem(result)) {
                            break;
                        }
                        input.removeItem(i, 1);
                        block.output(result);
                        written = true;
                        break;
                    }
                }
            }
            return written;
        }

        /**
         * The value of the first fitting item of the I/O blocks, the earliest bound first and each from its first
         * slot: a variable or constant item with the name and type, or a literal item of the type. A variable item
         * leaves an uninitialized variable item in the output, a constant item moves there unchanged, and a
         * literal item leaves an empty literal item of its type.
         */
        @Override
        public Optional<Value> input(String name, Type type) {
            String typeText = type.toString();
            for (IoBlockEntity block : ioBlocks()) {
                Optional<Value> value = take(block, name, typeText, type);
                if (value.isPresent()) {
                    return value;
                }
            }
            return Optional.empty();
        }

        private static Optional<Value> take(IoBlockEntity block, String name, String typeText, Type type) {
            SimpleContainer input = block.input();
            for (int i = 0; i < input.getContainerSize(); i++) {
                ItemStack stack = input.getItem(i);
                String json;
                if (stack.getItem() instanceof ValueItem) {
                    ValueItem.Contents contents = ValueItem.Contents.of(stack);
                    if (!contents.name().equals(name) || !contents.type().equals(typeText)) {
                        continue;
                    }
                    json = contents.value();
                } else if (stack.getItem() instanceof LiteralItem) {
                    LiteralItem.Contents contents = LiteralItem.Contents.of(stack);
                    if (contents.isEmpty() || !contents.type().equals(typeText)) {
                        continue;
                    }
                    json = contents.value();
                } else {
                    continue;
                }
                Value value;
                try {
                    value = ItemRules.value(json, type);
                } catch (C0deException e) {
                    continue; // An item whose value doesn't fit its type is left where it is
                }
                ItemStack taken = input.removeItem(i, 1);
                if (taken.getItem() instanceof LiteralItem) {
                    new LiteralItem.Contents(typeText, "").writeTo(taken);
                } else if (taken.getItem() instanceof ValueItem item && !item.isConstant()) {
                    taken = new ItemStack(C0deItems.VARIABLE_ITEM.get());
                }
                block.output(taken);
                return Optional.of(value);
            }
            return Optional.empty();
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
