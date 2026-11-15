package de.joernott.c0de.gametest;

import de.joernott.c0de.blockentity.TriggerBlockEntity;
import de.joernott.c0de.content.C0deBlocks;
import de.joernott.c0de.content.C0deItems;
import de.joernott.c0de.dimension.C0deDimension;
import de.joernott.c0de.item.ValueItem;
import de.joernott.c0de.process.Activator;
import de.joernott.c0de.sheet.SheetOwners;
import de.joernott.c0de.sheet.SheetValidation;
import de.joernott.c0de.vault.VaultFile;
import de.joernott.c0de.vault.VaultMessages;
import de.joernott.c0de.vault.VaultService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * The vault of the players and c0de/security (task 16.6 and 16.7, docs/concept/20-lib-security.md). Mock players
 * have no client, so a stand-in does what the client does: it creates, decrypts and changes the KeePass files with
 * the same library.
 */
public final class VaultTests {
    private VaultTests() {}

    /** The open vaults of the stand-in client, by player. */
    private static final Map<UUID, VaultFile> FILES = new ConcurrentHashMap<>();

    /** Answers like the client of a player; programs always pass the password, there is no dialog. */
    private static VaultMessages.Answer answer(UUID player, String action, VaultMessages.Request request) {
        try {
            switch (action) {
                case "create" -> {
                    VaultFile file = VaultFile.create(request.string(0).toCharArray());
                    FILES.put(player, file);
                    return VaultMessages.Answer.ok(file.bytes(), List.of());
                }
                case "open" -> {
                    VaultFile file = VaultFile.open(request.file(), request.string(0).toCharArray());
                    FILES.put(player, file);
                    return VaultMessages.Answer.ok(new byte[0], file.entries());
                }
                case "put" -> {
                    VaultFile file = FILES.get(player);
                    file.put(new VaultFile.Entry(request.string(0), request.string(1), request.string(2)));
                    return VaultMessages.Answer.ok(file.bytes(), List.of());
                }
                case "remove" -> {
                    VaultFile file = FILES.get(player);
                    file.remove(request.string(0));
                    return VaultMessages.Answer.ok(file.bytes(), List.of());
                }
                default -> {
                    return VaultMessages.Answer.of(VaultMessages.FAILED, "unknown action " + action);
                }
            }
        } catch (VaultFile.WrongPasswordException e) {
            return VaultMessages.Answer.of(VaultMessages.WRONG_PASSWORD, "");
        } catch (Exception e) {
            return VaultMessages.Answer.of(VaultMessages.FAILED, String.valueOf(e));
        }
    }

    private static final String SECRETS = """
            PROGRAM %s
            NOOP
            INCLUDE c0de/security
            NOOP
            FUNCTION run
                RESULT value STRING
                RESULT code INTEGER
            BEGIN
                VARIABLE problem ERROR
                CALL c0de/security/SET_SECRET problem
                    PARAMETER name "token" ;
                    PARAMETER value "abc" ;
                ASSIGN code problem.code ;
                IF code = 0 ;
                BEGIN
                    SECRET token STRING
                    ASSIGN value token ;
                END IF
            END FUNCTION
            """;

    private static ItemStack empty() {
        ItemStack item = new ItemStack(C0deItems.VARIABLE_ITEM.get());
        new ValueItem.Contents("", "", "").writeTo(item);
        return item;
    }

    private static String results(TriggerBlockEntity trigger) {
        StringBuilder results = new StringBuilder();
        for (int i = 0; i < trigger.output().getContainerSize(); i++) {
            ValueItem.Contents result = ValueItem.Contents.of(trigger.output().getItem(i));
            if (!result.name().isEmpty()) {
                results.append(result.name()).append('=').append(result.value()).append(' ');
            }
        }
        return results.toString().trim();
    }

    /**
     * A process uses the vault of the player who started it, if that player owns the sheet: the owner sets and
     * reads a secret; another player who starts the same program gets VAULT_UNAVAILABLE.
     */
    public static void secretsBelongToTheOwnerOfTheSheet(GameTestHelper helper) {
        ServerLevel c0de = helper.getLevel().getServer().getLevel(C0deDimension.LEVEL);
        SheetBuilder sheet = new SheetBuilder(c0de, 546).place(SECRETS.formatted("vaultowner"));
        SheetValidation.validate(c0de, 546);
        ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        ServerPlayer other = helper.makeMockServerPlayerInLevel();
        SheetOwners.get(c0de).check(owner, 546, true);
        helper.setBlock(BlockPos.ZERO, C0deBlocks.TRIGGER_BLOCK.get());
        TriggerBlockEntity trigger = (TriggerBlockEntity) TestCompat.blockEntity(helper, BlockPos.ZERO);
        trigger.configure(owner, "vaultowner", "run", 20);
        trigger.input().setItem(0, empty());
        trigger.input().setItem(1, empty());
        trigger.start(Activator.player(owner));
        helper.assertTrue(trigger.isRunning(), "The process didn't start: " + trigger.status());
        TestCompat.await(helper, () -> !trigger.isRunning(), () -> {
            String owners = results(trigger);
            helper.assertTrue(owners.equals("value=\"abc\" code=0"), "The owner got " + owners + " " + trigger.error());
            trigger.output().clearContent();
            trigger.input().setItem(0, empty());
            trigger.input().setItem(1, empty());
            trigger.start(Activator.player(other));
            helper.assertTrue(trigger.isRunning(), "The second process didn't start: " + trigger.status());
            TestCompat.await(helper, () -> !trigger.isRunning(), () -> {
                try {
                    String others = results(trigger);
                    helper.assertTrue(others.equals("value=\"\" code=314"), "Another player got " + others);
                } finally {
                    sheet.clear();
                    SheetValidation.validate(c0de, 546);
                }
                helper.succeed();
            });
        });
    }

    private static final String PERSISTENT = """
            PROGRAM vaultfile
            NOOP
            INCLUDE c0de/security
            NOOP
            FUNCTION store
                RESULT exists BOOLEAN
            BEGIN
                CALL c0de/security/CREATE_VAULT
                    PARAMETER password "pw" ;
                CALL c0de/security/SET_SECRET
                    PARAMETER name "token" ;
                    PARAMETER value "abc" ;
                    PARAMETER persistent TRUE ;
                CALL c0de/security/HAS_VAULT
                    RESULT result exists
            END FUNCTION
            NOOP
            FUNCTION load
                RESULT value STRING
            BEGIN
                CALL c0de/security/OPEN_VAULT
                    PARAMETER password "pw" ;
                SECRET token STRING
                ASSIGN value token ;
            END FUNCTION
            """;

    /**
     * The persistent vault: CREATE_VAULT and a persistent SET_SECRET store a KeePass file that only the password
     * opens; after the player left and joined again, OPEN_VAULT brings the secret back.
     */
    public static void persistentSecretsSurviveLeaving(GameTestHelper helper) {
        VaultService.standIn(VaultTests::answer);
        ServerLevel c0de = helper.getLevel().getServer().getLevel(C0deDimension.LEVEL);
        SheetBuilder sheet = new SheetBuilder(c0de, 548).place(PERSISTENT);
        SheetValidation.validate(c0de, 548);
        ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        SheetOwners.get(c0de).check(owner, 548, true);
        Path file = VaultService.file(helper.getLevel().getServer(), owner.getUUID());
        helper.setBlock(BlockPos.ZERO, C0deBlocks.TRIGGER_BLOCK.get());
        TriggerBlockEntity trigger = (TriggerBlockEntity) TestCompat.blockEntity(helper, BlockPos.ZERO);
        trigger.configure(owner, "vaultfile", "store", 20);
        trigger.input().setItem(0, empty());
        trigger.start(Activator.player(owner));
        helper.assertTrue(trigger.isRunning(), "The process didn't start: " + trigger.status());
        TestCompat.await(helper, () -> !trigger.isRunning(), () -> {
            String stored = results(trigger);
            helper.assertTrue(stored.equals("exists=true"), "store returned " + stored + " " + trigger.error());
            try {
                byte[] bytes = Files.readAllBytes(file);
                List<VaultFile.Entry> entries = VaultFile.open(bytes, "pw".toCharArray()).entries();
                helper.assertTrue(
                        entries.equals(List.of(new VaultFile.Entry("token", "STRING", "abc"))),
                        "The file holds " + entries);
            } catch (Exception e) {
                helper.fail("The file can't be read: " + e);
            }
            // Leaving and joining again: the vault is empty and closed
            VaultService.forget(owner.getUUID());
            FILES.remove(owner.getUUID());
            trigger.output().clearContent();
            trigger.configure(owner, "vaultfile", "load", 20);
            trigger.input().setItem(0, empty());
            trigger.start(Activator.player(owner));
            helper.assertTrue(trigger.isRunning(), "The second process didn't start: " + trigger.status());
            TestCompat.await(helper, () -> !trigger.isRunning(), () -> {
                try {
                    String loaded = results(trigger);
                    helper.assertTrue(loaded.equals("value=\"abc\""), "load returned " + loaded + " " + trigger.error());
                } finally {
                    sheet.clear();
                    SheetValidation.validate(c0de, 548);
                    try {
                        Files.deleteIfExists(file);
                    } catch (java.io.IOException e) {
                        // The file is in the world of the test server
                    }
                }
                helper.succeed();
            });
        });
    }
}
