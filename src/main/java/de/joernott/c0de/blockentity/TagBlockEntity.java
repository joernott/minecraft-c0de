package de.joernott.c0de.blockentity;

import de.joernott.c0de.compat.Nbt;
import de.joernott.c0de.content.C0deBlockEntities;
import de.joernott.c0de.core.edit.PropertyRules;
import de.joernott.c0de.core.line.Block;
import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.sheet.LevelSheet;
import java.time.ZoneId;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** The properties of a TAG: language and name (docs/concept/04-data.md). */
public class TagBlockEntity extends PropertiesBlockEntity {
    private String language = "";
    private String name = "";

    public TagBlockEntity(BlockPos pos, BlockState state) {
        super(C0deBlockEntities.TAG.get(), pos, state);
    }

    public String language() {
        return language;
    }

    public String name() {
        return name;
    }

    /** True if the tag is in a VARIABLE or CONSTANT line, where its name is a path. */
    public boolean isPath(Level level) {
        return LevelSheet.isOnSheet(level, worldPosition)
                && PropertyRules.isPathTag(
                        LevelSheet.view(level, worldPosition),
                        SheetGeometry.relativeX(worldPosition.getX()),
                        worldPosition.getZ());
    }

    @Override
    public String dialog() {
        return "tag";
    }

    @Override
    public Block toCore(ZoneId zone) {
        return new Block.TagBlock(language.isEmpty() ? null : language, name.isEmpty() ? null : name);
    }

    @Override
    public String displayText() {
        return language;
    }

    @Override
    public CompoundTag dialogData(ServerLevel level) {
        CompoundTag data = super.dialogData(level);
        data.putBoolean("path", isPath(level));
        return data;
    }

    @Override
    protected Optional<String> check(CompoundTag properties, ServerLevel level) {
        return PropertyRules.tag(Nbt.string(properties, "language"), Nbt.string(properties, "name"), isPath(level));
    }

    @Override
    protected void read(CompoundTag tag) {
        language = Nbt.string(tag, "language");
        name = Nbt.string(tag, "name");
    }

    @Override
    protected void write(CompoundTag tag) {
        tag.putString("language", language);
        tag.putString("name", name);
    }
}
