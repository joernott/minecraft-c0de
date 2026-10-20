package de.joernott.c0de.sheet;

import de.joernott.c0de.C0de;
import de.joernott.c0de.block.ValidatedCodeBlock;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.line.Sheet;
import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.core.validation.Problem;
import de.joernott.c0de.core.validation.Validation;
import de.joernott.c0de.core.validation.Validator;
import de.joernott.c0de.dimension.C0deDimension;
import de.joernott.c0de.library.ModLibraries;
import de.joernott.c0de.platform.Platform;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Validates the programs on the sheets while players write them. When a c0de block on a sheet is placed,
 * removed or changed, its sheet is validated again at the end of the server tick, so that many changes in one
 * tick (e.g. by a command) cause only one validation. The whole program is validated, because a change can
 * affect lines far away (e.g. a renamed variable). The {@code valid} block states are only changed where the
 * result differs.
 */
public final class SheetValidation {
    /** The sheets that changed in this tick. Only used on the server thread. */
    private static final Set<Integer> CHANGED = new LinkedHashSet<>();

    private SheetValidation() {}

    /** Registers the validation at the end of every server tick. Called once when the mod is initialized. */
    public static void register() {
        Platform.INSTANCE.onServerTickEnd(SheetValidation::tick);
    }

    /** Notes that a block on the code layer of a sheet has changed. */
    public static void changed(Level level, BlockPos pos) {
        if (!level.isClientSide() && pos.getY() == SheetGeometry.CODE_Y && LevelSheet.isOnSheet(level, pos)) {
            CHANGED.add(LevelSheet.sheetOf(pos));
        }
    }

    private static void tick(MinecraftServer server) {
        if (CHANGED.isEmpty()) {
            return;
        }
        ServerLevel level = server.getLevel(C0deDimension.LEVEL);
        List<Integer> sheets = List.copyOf(CHANGED);
        CHANGED.clear();
        if (level != null) {
            sheets.forEach(sheet -> validate(level, sheet));
        }
    }

    /**
     * Validates a sheet, updates the program registry and the {@code valid} block states.
     *
     * @return the validation, or empty if a part of the program is in a chunk that is not loaded
     */
    public static Optional<Validation> validate(ServerLevel level, int sheet) {
        Optional<Sheet> lines = LevelSheet.read(level, sheet);
        if (lines.isEmpty()) {
            C0de.LOGGER.debug("Sheet {} not validated: a part of it is not loaded", sheet);
            return Optional.empty();
        }
        Validation validation = Validator.validate(lines.get(), ModLibraries.registry());
        ProgramRegistry registry = ProgramRegistry.get(level);
        String name = validation.programName();
        OptionalInt other = name == null ? OptionalInt.empty() : registry.sheetOf(name);
        if (other.isPresent() && other.getAsInt() != sheet) {
            validation = validation.withProblem(new Problem(
                    0,
                    ErrorCode.SYNTAX_ERROR_PLACEMENT,
                    "The name " + name + " is used by the program on sheet " + other.getAsInt()));
            registry.update(sheet, null, List.of());
        } else {
            registry.update(sheet, name, validation.functions());
        }
        apply(level, sheet, validation);
        return Optional.of(validation);
    }

    private static void apply(ServerLevel level, int sheet, Validation validation) {
        for (int z = 0; z < validation.endLine(); z++) {
            setLine(level, sheet, z, validation.isValid(z));
        }
        // The lines after the empty line that ends the program don't belong to it
        LevelSheet.readGroup(level, sheet, validation.endLine() + 1)
                .ifPresent(lines -> lines.forEach(line -> setLine(level, sheet, line.z(), false)));
    }

    private static void setLine(ServerLevel level, int sheet, int z, boolean valid) {
        for (int x = SheetGeometry.CODE_MIN_X; x <= SheetGeometry.CODE_MAX_X; x++) {
            BlockPos pos = LevelSheet.codePos(sheet, x, z);
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof ValidatedCodeBlock && state.getValue(ValidatedCodeBlock.VALID) != valid) {
                level.setBlock(pos, state.setValue(ValidatedCodeBlock.VALID, valid), Block.UPDATE_CLIENTS);
            }
        }
    }
}
