package de.joernott.c0de.io;

import de.joernott.c0de.core.io.TextScreen;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

/** Screens in NBT: for the block entities of displays and laptops and for the synchronization with clients. */
public final class ScreenNbt {
    private ScreenNbt() {}

    public static CompoundTag write(TextScreen screen) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("width", screen.width());
        tag.putInt("height", screen.height());
        tag.put("rows", rows(screen.rows()));
        tag.put("history", rows(screen.history()));
        tag.putInt("row", screen.row());
        tag.putInt("column", screen.column());
        tag.putInt("foreground", screen.foreground());
        tag.putInt("background", screen.background());
        return tag;
    }

    /** The screen with only the newest rows of its history, for an update of a screen the client already has. */
    public static CompoundTag update(TextScreen screen, int newHistoryRows) {
        CompoundTag tag = write(screen);
        List<TextScreen.Row> history = screen.history();
        tag.put("history", rows(history.subList(Math.max(0, history.size() - newHistoryRows), history.size())));
        tag.putBoolean("append", true);
        return tag;
    }

    /** Applies a screen written by {@link #write} or {@link #update} to the screen of a client. */
    public static void apply(TextScreen screen, CompoundTag tag) {
        List<TextScreen.Row> history = new ArrayList<>();
        if (tag.getBoolean("append")) {
            history.addAll(screen.history());
        }
        history.addAll(rows(tag.getList("history", Tag.TAG_COMPOUND)));
        screen.restore(
                rows(tag.getList("rows", Tag.TAG_COMPOUND)),
                history,
                tag.getInt("row"),
                tag.getInt("column"),
                tag.getInt("foreground"),
                tag.getInt("background"));
    }

    /** A screen of the given size with the content of the tag; content that doesn't fit is cut off. */
    public static TextScreen read(CompoundTag tag, int width, int height, int historySize) {
        TextScreen screen = new TextScreen(width, height, historySize);
        if (tag.contains("rows")) {
            screen.restore(
                    rows(tag.getList("rows", Tag.TAG_COMPOUND)),
                    rows(tag.getList("history", Tag.TAG_COMPOUND)),
                    tag.getInt("row"),
                    tag.getInt("column"),
                    tag.contains("foreground") ? tag.getInt("foreground") : TextScreen.DEFAULT_FOREGROUND,
                    tag.getInt("background"));
        }
        return screen;
    }

    private static ListTag rows(List<TextScreen.Row> rows) {
        ListTag list = new ListTag();
        for (TextScreen.Row row : rows) {
            CompoundTag entry = new CompoundTag();
            entry.putIntArray("characters", row.characters());
            entry.putByteArray("foregrounds", row.foregrounds());
            entry.putByteArray("backgrounds", row.backgrounds());
            list.add(entry);
        }
        return list;
    }

    private static List<TextScreen.Row> rows(ListTag list) {
        List<TextScreen.Row> rows = new ArrayList<>();
        for (Tag element : list) {
            CompoundTag entry = (CompoundTag) element;
            rows.add(new TextScreen.Row(
                    entry.getIntArray("characters"), entry.getByteArray("foregrounds"), entry.getByteArray("backgrounds")));
        }
        return rows;
    }
}
