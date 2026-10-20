package de.joernott.c0de.gametest;

import de.joernott.c0de.C0de;
import de.joernott.c0de.block.CodeBlock;
import de.joernott.c0de.block.ValidatedCodeBlock;
import de.joernott.c0de.blockentity.LabelBlockEntity;
import de.joernott.c0de.blockentity.LiteralBlockEntity;
import de.joernott.c0de.blockentity.OperatorBlockEntity;
import de.joernott.c0de.blockentity.PropertiesBlockEntity;
import de.joernott.c0de.blockentity.TagBlockEntity;
import de.joernott.c0de.content.C0deBlocks;
import de.joernott.c0de.content.CodeBlockType;
import de.joernott.c0de.core.edit.LabelKind;
import de.joernott.c0de.core.edit.LabelMode;
import de.joernott.c0de.core.line.Block;
import de.joernott.c0de.core.line.Operator;
import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.dialog.DialogService;
import de.joernott.c0de.dimension.C0deDimension;
import de.joernott.c0de.network.OpenDialogPayload;
import de.joernott.c0de.network.SavePropertiesPayload;
import de.joernott.c0de.sheet.LevelSheet;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;

/** GameTests of the language blocks, their properties and dialogs (sprint 6). */
public final class CodeBlockTests {
    /** A sheet far away from the sheets other tests look at. */
    private static final int SHEET = 500;

    private CodeBlockTests() {}

    public static void allBlocksHaveItemsLootTablesAndRecipes(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        for (CodeBlockType type : CodeBlockType.values()) {
            var id = C0de.id(type.id());
            var block = BuiltInRegistries.BLOCK.get(id);
            helper.assertTrue(block instanceof CodeBlock code && code.type() == type, id + " is not registered");
            helper.assertTrue(
                    BuiltInRegistries.ITEM.get(id).asItem() != net.minecraft.world.item.Items.AIR, id + " has no item");
            LootTable loot = server.reloadableRegistries()
                    .getLootTable(ResourceKey.create(Registries.LOOT_TABLE, id.withPrefix("blocks/")));
            helper.assertTrue(loot != LootTable.EMPTY, id + " has no loot table");
            helper.assertTrue(server.getRecipeManager().byKey(id).isPresent(), id + " has no recipe");
            helper.assertTrue(
                    block.defaultBlockState().hasProperty(ValidatedCodeBlock.VALID) == type.isValidated(),
                    id + " has the wrong block states");
        }
        for (String floor : List.of(
                "white_c0de_bedrock", "light_green_c0de_bedrock", "black_c0de_bedrock", "cursor_c0de_bedrock")) {
            helper.assertTrue(
                    BuiltInRegistries.ITEM.get(C0de.id(floor)) instanceof net.minecraft.world.item.BlockItem,
                    floor + " has no item for picking it in creative mode");
        }
        for (String item : List.of("type_base", "structural_base")) {
            helper.assertTrue(server.getRecipeManager().byKey(C0de.id(item)).isPresent(), item + " has no recipe");
        }
        helper.succeed();
    }

    private static <T extends BlockEntity> T place(GameTestHelper helper, CodeBlockType type, Class<T> entityClass) {
        helper.setBlock(BlockPos.ZERO, C0deBlocks.get(type));
        BlockEntity entity = helper.getBlockEntity(BlockPos.ZERO);
        helper.assertTrue(entityClass.isInstance(entity), type + " has no " + entityClass.getSimpleName());
        return entityClass.cast(entity);
    }

    private static Optional<String> save(GameTestHelper helper, ServerPlayer player, CompoundTag properties) {
        BlockPos pos = helper.absolutePos(BlockPos.ZERO);
        return DialogService.save(player, pos, new SavePropertiesPayload(pos, properties));
    }

    private static ServerPlayer playerAtTheBlock(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos pos = helper.absolutePos(BlockPos.ZERO);
        player.teleportTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 1.5);
        return player;
    }

    public static void literalPropertiesAreCheckedSavedAndSynchronized(GameTestHelper helper) {
        LiteralBlockEntity literal = place(helper, CodeBlockType.LITERAL, LiteralBlockEntity.class);
        ServerPlayer player = playerAtTheBlock(helper);

        OpenDialogPayload dialog = DialogService.payload(player, helper.absolutePos(BlockPos.ZERO));
        helper.assertTrue(dialog != null && dialog.dialog().equals("literal"), "No literal dialog: " + dialog);
        helper.assertTrue(!dialog.readOnly(), "The dialog is read-only");

        CompoundTag invalid = new CompoundTag();
        invalid.putString("type", "INTEGER");
        invalid.putString("value", "4.2");
        helper.assertTrue(save(helper, player, invalid).isPresent(), "An invalid INTEGER was saved");
        helper.assertTrue(literal.type().isEmpty(), "An invalid value changed the literal");

        CompoundTag valid = new CompoundTag();
        valid.putString("type", "INTEGER");
        valid.putString("value", "42");
        helper.assertTrue(save(helper, player, valid).isEmpty(), "A valid INTEGER was not saved");
        helper.assertTrue(
                literal.toCore(ZoneOffset.UTC).equals(new Block.LiteralBlock(new IntegerValue(42))),
                "The literal is " + literal.toCore(ZoneOffset.UTC));
        helper.assertTrue(literal.displayText().equals("42"), "The literal shows " + literal.displayText());

        // The properties are stored and sent to the clients
        var registries = helper.getLevel().registryAccess();
        CompoundTag update = literal.getUpdateTag(registries);
        helper.assertTrue(update.getString("value").equals("42"), "The update tag lacks the value: " + update);
        BlockEntity copy = BlockEntity.loadStatic(
                literal.getBlockPos(), literal.getBlockState(), literal.saveWithFullMetadata(registries), registries);
        helper.assertTrue(
                copy instanceof LiteralBlockEntity loaded && loaded.value().equals("42"),
                "The value is not saved with the block");
        helper.succeed();
    }

    public static void operatorAndTagProperties(GameTestHelper helper) {
        ServerPlayer player = playerAtTheBlock(helper);
        OperatorBlockEntity operator = place(helper, CodeBlockType.OPERATOR, OperatorBlockEntity.class);
        CompoundTag negate = new CompoundTag();
        negate.putString("operator", Operator.NEGATE.name());
        helper.assertTrue(save(helper, player, negate).isPresent(), "NEGATE can be chosen");
        CompoundTag concat = new CompoundTag();
        concat.putString("operator", Operator.CONCAT.name());
        helper.assertTrue(save(helper, player, concat).isEmpty(), "CONCAT was not saved");
        helper.assertTrue(operator.operator() == Operator.CONCAT, "The operator is " + operator.operator());

        TagBlockEntity tag = place(helper, CodeBlockType.TAG, TagBlockEntity.class);
        CompoundTag element = new CompoundTag();
        element.putString("language", "yaml");
        element.putString("name", "a.b");
        helper.assertTrue(save(helper, player, element).isPresent(), "A struct element tag with a dot was saved");
        element.putString("name", "name");
        helper.assertTrue(save(helper, player, element).isEmpty(), "A valid tag was not saved");
        helper.assertTrue(tag.toCore(ZoneOffset.UTC).equals(new Block.TagBlock("yaml", "name")), "Wrong tag");
        helper.succeed();
    }

    public static void blocksFarAwayCantBeChanged(GameTestHelper helper) {
        place(helper, CodeBlockType.COMMENT, PropertiesBlockEntity.class);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos pos = helper.absolutePos(BlockPos.ZERO);
        player.teleportTo(pos.getX() + 50, pos.getY(), pos.getZ());
        CompoundTag comment = new CompoundTag();
        comment.putString("text", "hello");
        helper.assertTrue(save(helper, player, comment).isPresent(), "A block far away was changed");
        helper.succeed();
    }

    /** Places blocks on a sheet in the c0de dimension and checks the context of labels and tags there. */
    public static void labelsAndTagsUseTheirPositionOnTheSheet(GameTestHelper helper) {
        ServerLevel level = helper.getLevel().getServer().getLevel(C0deDimension.LEVEL);
        helper.assertTrue(level != null, "The c0de dimension doesn't exist");
        SheetBuilder sheet = new SheetBuilder(level, SHEET)
                .place(
                        """
                        PROGRAM demo
                        NOOP
                            INCLUDE c0de/math
                        VARIABLE port INTEGER TAG(json: "server.port")
                        NOOP
                        FUNCTION main
                        BEGIN
                            CALL main
                        END FUNCTION
                        """);
        try {
            LabelBlockEntity program = (LabelBlockEntity) level.getBlockEntity(sheet.pos(6, 0));
            helper.assertTrue(program.kind().equals(LabelKind.PROGRAM.name()), "The program label is " + program.kind());
            TagBlockEntity tag = (TagBlockEntity) level.getBlockEntity(sheet.pos(8, 3));
            helper.assertTrue(tag.isPath(level), "The tag of a variable is not a path");

            LabelBlockEntity call = (LabelBlockEntity) level.getBlockEntity(sheet.pos(7, 7));
            CompoundTag data = call.dialogData(level);
            helper.assertTrue(
                    data.getString("mode").equals(LabelMode.FUNCTION.name()), "The CALL label dialog is " + data);
            CompoundTag function = new CompoundTag();
            function.putString("name", "c0de/math/SIN");
            helper.assertTrue(call.apply(function, level).isEmpty(), "The function was not saved");
            helper.assertTrue(
                    call.parent().equals("c0de/math") && call.name().equals("SIN"),
                    "The function is " + call.parent() + " / " + call.name());
            function.putString("name", "main");
            helper.assertTrue(call.apply(function, level).isEmpty(), "The function of the program was not saved");
            helper.assertTrue(call.parent().equals("demo"), "The parent of a program function is " + call.parent());
            helper.assertTrue(
                    LevelSheet.toCore(level, sheet.pos(7, 7)).equals(new Block.LabelBlock("main")),
                    "The sheet reads " + LevelSheet.toCore(level, sheet.pos(7, 7)));
        } finally {
            sheet.clear();
        }
        helper.succeed();
    }
}
