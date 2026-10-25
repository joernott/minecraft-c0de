package de.joernott.c0de.blockentity;

import de.joernott.c0de.content.C0deBlockEntities;
import de.joernott.c0de.core.edit.LabelContext;
import de.joernott.c0de.core.edit.LabelKind;
import de.joernott.c0de.core.edit.LabelMode;
import de.joernott.c0de.core.edit.PropertyRules;
import de.joernott.c0de.core.line.Block;
import de.joernott.c0de.core.line.IndexText;
import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.dialog.DialogOptions;
import de.joernott.c0de.sheet.LevelSheet;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** The properties of a LABEL: kind, parent, name and index (docs/concept/02-language-basics.md). */
public class LabelBlockEntity extends PropertiesBlockEntity {
    private String kind = "";
    private String parent = "";
    private String name = "";
    private String index = "";

    public LabelBlockEntity(BlockPos pos, BlockState state) {
        super(C0deBlockEntities.LABEL.get(), pos, state);
    }

    public String kind() {
        return kind;
    }

    public String parent() {
        return parent;
    }

    public String name() {
        return name;
    }

    /** The index in its text form, e.g. {@code [i][2]}. */
    public String index() {
        return index;
    }

    /** The context of the label at its position: on a sheet from the blocks around it, a reference elsewhere. */
    public LabelContext context(Level level) {
        if (!LevelSheet.isOnSheet(level, worldPosition)) {
            return new LabelContext(LabelKind.REFERENCE, LabelMode.REFERENCE, null);
        }
        return LabelContext.of(
                LevelSheet.view(level, worldPosition),
                SheetGeometry.relativeX(worldPosition.getX()),
                worldPosition.getZ());
    }

    /** Initializes the kind and parent from the position of a placed label. */
    public void placed(Level level) {
        LabelContext context = context(level);
        kind = context.kind().name();
        parent = context.parent() == null ? "" : context.parent();
        changed();
    }

    @Override
    public String dialog() {
        return "label";
    }

    @Override
    public Block toCore(ZoneId zone) {
        if (name.isEmpty()) {
            return new Block.LabelBlock(null);
        }
        return new Block.LabelBlock(qualifiedName(), PropertyRules.index(index).entries());
    }

    @Override
    public String displayText() {
        // The index shows which element is meant, e.g. names[i] (task 11.11.5)
        return name.substring(name.lastIndexOf('/') + 1) + index;
    }

    /** The name as used in the program: functions of libraries are called by their qualified names. */
    private String qualifiedName() {
        return kind.equals(LabelKind.FUNCTION.name()) && parent.contains("/") ? parent + "/" + name : name;
    }

    @Override
    public CompoundTag dialogData(ServerLevel level) {
        LabelContext context = context(level);
        CompoundTag data = new CompoundTag();
        data.putString("kind", context.kind().name());
        data.putString("name", qualifiedName());
        data.putString("index", index);
        data.putString("mode", context.mode().name());
        ListTag options = new ListTag();
        for (String option : DialogOptions.forLabel(context.mode(), level, worldPosition)) {
            options.add(StringTag.valueOf(option));
        }
        data.put("options", options);
        return data;
    }

    @Override
    protected Optional<String> check(CompoundTag properties, ServerLevel level) {
        return PropertyRules.label(
                context(level).mode(), properties.getString("name"), properties.getString("index"));
    }

    /** Sets kind and parent from the current position, because the blocks around the label may have changed. */
    @Override
    public Optional<String> apply(CompoundTag properties, ServerLevel level) {
        Optional<String> problem = check(properties, level);
        if (problem.isPresent()) {
            return problem;
        }
        LabelContext context = context(level);
        String entered = properties.getString("name");
        kind = context.kind().name();
        if (context.mode() == LabelMode.FUNCTION) {
            List<String> split = PropertyRules.splitFunction(
                    entered,
                    DialogOptions.builtinFunctions(),
                    context.parent() == null ? "" : context.parent());
            parent = split.get(0);
            name = split.get(1);
        } else {
            parent = context.parent() == null ? "" : context.parent();
            name = entered;
        }
        // The index is stored in its normalized text form
        index = context.mode().allowsIndex()
                ? IndexText.format(PropertyRules.index(properties.getString("index")).entries())
                : "";
        changed();
        return Optional.empty();
    }

    @Override
    protected void read(CompoundTag tag) {
        kind = tag.getString("kind");
        parent = tag.getString("parent");
        name = tag.getString("name");
        index = tag.getString("index");
    }

    @Override
    protected void write(CompoundTag tag) {
        tag.putString("kind", kind);
        tag.putString("parent", parent);
        tag.putString("name", name);
        tag.putString("index", index);
    }
}
