package de.joernott.c0de.gametest;

import de.joernott.c0de.block.ValidatedCodeBlock;
import de.joernott.c0de.blockentity.LabelBlockEntity;
import de.joernott.c0de.compat.Nbt;
import de.joernott.c0de.content.C0deBlocks;
import de.joernott.c0de.content.C0deItems;
import de.joernott.c0de.content.CodeBlockType;
import de.joernott.c0de.core.edit.LabelMode;
import de.joernott.c0de.core.validation.Validation;
import de.joernott.c0de.dimension.C0deDimension;
import de.joernott.c0de.sheet.ProgramRegistry;
import de.joernott.c0de.sheet.SheetValidation;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** GameTests of the validation of sheets (sprint 7). Every test uses its own sheet. */
public final class ValidationTests {
    private static final String PROGRAM =
            """
            PROGRAM validation
            NOOP
            INCLUDE c0de/math
            VARIABLE angle FLOAT 1.5
            NOOP
            FUNCTION main
                PARAMETER count INTEGER
            BEGIN
                VARIABLE local INTEGER
                CALL c0de/math/SIN
                    PARAMETER value angle ;
                    RESULT result angle
            END FUNCTION
            """;

    private ValidationTests() {}

    private static ServerLevel c0de(GameTestHelper helper) {
        ServerLevel level = helper.getLevel().getServer().getLevel(C0deDimension.LEVEL);
        helper.assertTrue(level != null, "The c0de dimension doesn't exist");
        return level;
    }

    private static boolean valid(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.hasProperty(ValidatedCodeBlock.VALID) && state.getValue(ValidatedCodeBlock.VALID);
    }

    public static void programsAreValidatedAndRegistered(GameTestHelper helper) {
        ServerLevel level = c0de(helper);
        SheetBuilder sheet = new SheetBuilder(level, 501).place(PROGRAM);
        try {
            Validation validation = SheetValidation.validate(level, 501).orElseThrow();
            helper.assertTrue(validation.isValid(), "The program is invalid: " + validation.problems());
            helper.assertTrue(valid(level, sheet.pos(5, 5)), "The FUNCTION block is not shown as valid");
            helper.assertTrue(valid(level, sheet.pos(8, 10)), "The PARAMETER label is not shown as valid");
            ProgramRegistry registry = ProgramRegistry.get(level);
            helper.assertTrue(registry.sheetOf("validation").orElse(-1) == 501, "The program is not registered");
            helper.assertTrue(
                    registry.functions("validation").stream()
                            .anyMatch(f -> f.name().equals("main") && f.parameters().equals(List.of("count"))),
                    "The functions are not registered: " + registry.functions("validation"));

            // Without END, the BEGIN line has no match and the whole function is invalid
            level.removeBlock(sheet.pos(5, 12), false);
            validation = SheetValidation.validate(level, 501).orElseThrow();
            helper.assertTrue(!validation.isValid(7), "The BEGIN line without END is valid");
            helper.assertTrue(!valid(level, sheet.pos(5, 7)), "The BEGIN block is shown as valid");
            helper.assertTrue(valid(level, sheet.pos(5, 0)), "The PROGRAM block is not valid any more");
            // The line numbers show the function as incomplete (task 11.3)
            helper.assertTrue(
                    level.getBlockState(sheet.pos(5, 7)).getValue(ValidatedCodeBlock.INCOMPLETE),
                    "The BEGIN block is not marked as incomplete");
            helper.assertTrue(
                    !level.getBlockState(sheet.pos(5, 0)).getValue(ValidatedCodeBlock.INCOMPLETE),
                    "The PROGRAM block is marked as incomplete");
        } finally {
            sheet.clear();
            SheetValidation.validate(level, 501);
        }
        helper.assertTrue(
                ProgramRegistry.get(level).sheetOf("validation").isEmpty(), "A removed program is still registered");
        helper.succeed();
    }

    public static void changesAreValidatedAtTheEndOfTheTick(GameTestHelper helper) {
        ServerLevel level = c0de(helper);
        SheetBuilder sheet = new SheetBuilder(level, 502).place("PROGRAM ticking\nNOOP\n");
        helper.runAfterDelay(2, () -> {
            try {
                helper.assertTrue(valid(level, sheet.pos(5, 0)), "The PROGRAM block was not validated");
                // A second PROGRAM line makes the program invalid
                sheet.place(new de.joernott.c0de.core.line.Block.KeywordBlock(
                        de.joernott.c0de.core.line.Keyword.PROGRAM), 5, 2);
            } catch (RuntimeException e) {
                sheet.clear();
                throw e;
            }
            helper.runAfterDelay(2, () -> {
                try {
                    helper.assertTrue(!valid(level, sheet.pos(5, 2)), "The second PROGRAM block is valid");
                } finally {
                    sheet.clear();
                }
                helper.succeed();
            });
        });
    }

    public static void programNamesAreUnique(GameTestHelper helper) {
        ServerLevel level = c0de(helper);
        SheetBuilder first = new SheetBuilder(level, 503).place("PROGRAM twin\nNOOP\n");
        SheetBuilder second = new SheetBuilder(level, 504).place("PROGRAM twin\nNOOP\n");
        try {
            helper.assertTrue(SheetValidation.validate(level, 503).orElseThrow().isValid(), "The first twin is invalid");
            Validation validation = SheetValidation.validate(level, 504).orElseThrow();
            helper.assertTrue(
                    validation.problem(0).map(p -> p.message().contains("sheet 503")).orElse(false),
                    "The second twin is valid: " + validation.problems());
            helper.assertTrue(!valid(level, second.pos(6, 0)), "The name of the second twin is shown as valid");
        } finally {
            first.clear();
            second.clear();
            SheetValidation.validate(level, 503);
            SheetValidation.validate(level, 504);
        }
        helper.succeed();
    }

    public static void placementRulesPreventMistakes(GameTestHelper helper) {
        ServerLevel level = c0de(helper);
        SheetBuilder sheet = new SheetBuilder(level, 505).place("PROGRAM rules\nNOOP\n");
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            helper.assertTrue(
                    placement(level, player, CodeBlockType.FUNCTION, sheet.pos(6, 2)) == null,
                    "FUNCTION can be placed at x=6");
            helper.assertTrue(
                    placement(level, player, CodeBlockType.FUNCTION, sheet.pos(5, 2)) != null,
                    "FUNCTION can't be placed after the NOOP line");
            helper.assertTrue(
                    placement(level, player, CodeBlockType.LABEL, sheet.pos(6, 1)) == null,
                    "A LABEL can be placed in a NOOP line");
            helper.assertTrue(
                    placement(level, player, CodeBlockType.PARAMETER, sheet.pos(5, 2).west(2)) == null,
                    "A PARAMETER can be placed at x=3");
        } finally {
            sheet.clear();
            SheetValidation.validate(level, 505);
        }
        helper.succeed();
    }

    private static BlockState placement(ServerLevel level, ServerPlayer player, CodeBlockType type, BlockPos pos) {
        BlockPlaceContext context = new BlockPlaceContext(
                level,
                player,
                InteractionHand.MAIN_HAND,
                new ItemStack(C0deItems.CODE_BLOCKS.get(type).get()),
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false)) {
            // The constructor with a level is protected; the player is not in the c0de dimension
        };
        return C0deBlocks.get(type).getStateForPlacement(context);
    }

    public static void labelDialogsListTheNamesOfTheProgram(GameTestHelper helper) {
        ServerLevel level = c0de(helper);
        SheetBuilder sheet = new SheetBuilder(level, 506).place(PROGRAM.replace("validation", "dialogs"));
        try {
            // The label "angle" of the PARAMETER line of the CALL: an expression
            CompoundTag reference = ((LabelBlockEntity) level.getBlockEntity(sheet.pos(9, 10))).dialogData(level);
            helper.assertTrue(
                    Nbt.string(reference, "mode").equals(LabelMode.REFERENCE.name()), "Not a reference: " + reference);
            String names = Nbt.list(reference, "options", Tag.TAG_STRING).toString();
            for (String name : List.of("local", "count", "angle", "c0de/math/PI")) {
                helper.assertTrue(names.contains("\"" + name + "\""), name + " is not listed: " + names);
            }
            // The label "value" after PARAMETER in the CALL: the parameters of SIN
            CompoundTag parameter = ((LabelBlockEntity) level.getBlockEntity(sheet.pos(8, 10))).dialogData(level);
            helper.assertTrue(
                    Nbt.string(parameter, "mode").equals(LabelMode.CALL_PARAMETER.name())
                            && Nbt.list(parameter, "options", Tag.TAG_STRING).size() == 1,
                    "The parameters of SIN are not listed: " + parameter);
            // The label after CALL: the functions, including the program's own
            CompoundTag function = ((LabelBlockEntity) level.getBlockEntity(sheet.pos(7, 9))).dialogData(level);
            String functions = Nbt.list(function, "options", Tag.TAG_STRING).toString();
            helper.assertTrue(
                    functions.contains("\"main\"") && functions.contains("\"c0de/math/SIN\""),
                    "The functions are not listed: " + functions);
        } finally {
            sheet.clear();
            SheetValidation.validate(level, 506);
        }
        helper.succeed();
    }

    /** A program from before the registry existed is registered when the chunk of its PROGRAM line loads. */
    public static void existingProgramsAreRegisteredWhenTheirChunksLoad(GameTestHelper helper) {
        ServerLevel level = c0de(helper);
        SheetBuilder sheet = new SheetBuilder(level, 507).place("PROGRAM veteran\nNOOP\n");
        ProgramRegistry registry = ProgramRegistry.get(level);
        helper.runAfterDelay(2, () -> {
            // Forgets the program, as if it was written by an older version of the mod
            registry.update(507, null, List.of());
            SheetValidation.chunkLoaded(level, de.joernott.c0de.compat.Compat.chunk(sheet.pos(5, 0)));
            helper.runAfterDelay(2, () -> {
                try {
                    helper.assertTrue(
                            registry.sheetOf("veteran").orElse(-1) == 507, "The program was not registered");
                } finally {
                    sheet.clear();
                    SheetValidation.validate(level, 507);
                }
                helper.succeed();
            });
        });
    }
}
