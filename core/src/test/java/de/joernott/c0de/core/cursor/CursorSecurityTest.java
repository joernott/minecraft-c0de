package de.joernott.c0de.core.cursor;

import static de.joernott.c0de.core.cursor.CursorTestSupport.crashed;
import static de.joernott.c0de.core.cursor.CursorTestSupport.finished;
import static de.joernott.c0de.core.cursor.CursorTestSupport.integer;
import static de.joernott.c0de.core.cursor.CursorTestSupport.string;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.io.Console;
import de.joernott.c0de.core.library.MemoryVault;
import de.joernott.c0de.core.library.Vault;
import de.joernott.c0de.core.library.standard.StandardLibraries;
import de.joernott.c0de.core.value.BooleanValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** SECRET, sensitive values, hidden INPUT and WRITE and the library c0de/security (docs/concept/20-lib-security.md). */
class CursorSecurityTest {
    /** A host with a vault and a console that records what INPUT and WRITE get. */
    private static final class Host implements Vault.Provider, Console {
        final Vault vault;
        final List<String> log = new ArrayList<>();

        Host(Vault vault) {
            this.vault = vault;
        }

        @Override
        public Vault vault() {
            return vault;
        }

        @Override
        public Optional<Value> input(String name, Type type, boolean hide) {
            log.add("input " + name + " hide=" + hide);
            return Optional.of(new StringValue("typed"));
        }

        @Override
        public boolean write(String name, Value value, boolean hidden) {
            log.add("write " + name + "=" + value.text() + " hidden=" + hidden);
            return true;
        }
    }

    private static Cursor cursor(String program, Object host) {
        CursorSettings settings = new CursorSettings(
                CursorSettings.DEFAULT_STACK_SIZE,
                StandardLibraries.registry(),
                CursorTestSupport.CLOCK,
                ZoneOffset.UTC,
                host,
                42);
        return new Cursor(CursorTestSupport.program(program), "main", Map.of(), settings);
    }

    private static Cursor run(String program, Object host) {
        Cursor cursor = cursor(program, host);
        cursor.run(10_000);
        assertTrue(cursor.ended(), "the program didn't end");
        return cursor;
    }

    private static Vault vaultWithKey() {
        return new MemoryVault().withSecret(new Vault.Secret("apiKey", new StringValue("abc"), false));
    }

    @Test
    void aSecretHasTheValueOfTheVault() {
        Map<String, Value> results = finished(run("""
                SECRET apiKey STRING
                NOOP
                FUNCTION main
                    RESULT url STRING
                BEGIN
                    ASSIGN url "https://example.org/?key=" ⊕ apiKey ;
                END FUNCTION
                """, new Host(vaultWithKey())));
        assertEquals(string("https://example.org/?key=abc"), results.get("url"));
    }

    @Test
    void aMissingSecretOrVaultOrTypeIsAnError() {
        String program = """
                FUNCTION main
                BEGIN
                    SECRET apiKey %s
                END FUNCTION
                """;
        crashed(run(program.formatted("STRING"), new Host(new MemoryVault())), ErrorCode.SECRET_NOT_FOUND, 4);
        crashed(run(program.formatted("STRING"), null), ErrorCode.VAULT_UNAVAILABLE, 4);
        crashed(run(program.formatted("INTEGER"), new Host(vaultWithKey())), ErrorCode.TYPE_MISMATCH, 4);
    }

    @Test
    void aSecretCantBeChanged() {
        crashed(run("""
                        FUNCTION main
                        BEGIN
                            SECRET apiKey STRING
                            ASSIGN apiKey "x" ;
                        END FUNCTION
                        """, new Host(vaultWithKey())), ErrorCode.SYNTAX_ERROR_ASSIGN, 5);
    }

    @Test
    void theDebugOutputHidesSensitiveValues() {
        Cursor cursor = cursor("""
                STRUCT login
                BEGIN
                    user STRING
                    password STRING TAG(security: "sensitive")
                END STRUCT
                NOOP
                FUNCTION main
                BEGIN
                    SECRET apiKey STRING
                    VARIABLE token STRING "t0k3n" TAG(security: "sensitive")
                    VARIABLE plain STRING "visible"
                    VARIABLE account STRUCT login
                    BEGIN
                        user "alex"
                        password "pw"
                    END VARIABLE
                    CALL INPUT
                        PARAMETER variable plain ;
                END FUNCTION
                """, (Vault.Provider) CursorSecurityTest::vaultWithKey);
        cursor.run(20);
        String stack = String.join("\n", CursorInspection.stack(cursor));
        assertEquals(CursorState.WAITING, cursor.state(), () -> "error: " + cursor.error());
        assertTrue(stack.contains("CONSTANT apiKey STRING = ***"), stack);
        assertTrue(stack.contains("VARIABLE token STRING = ***"), stack);
        assertTrue(stack.contains("VARIABLE plain STRING = visible"), stack);
        assertTrue(stack.contains("{user: alex, password: ***}"), stack);
        assertFalse(stack.contains("abc") || stack.contains("t0k3n") || stack.contains("pw}"), stack);
    }

    @Test
    void theLibrarySetsAndDeletesSecrets() {
        MemoryVault vault = new MemoryVault();
        Map<String, Value> results = finished(run("""
                INCLUDE c0de/security
                NOOP
                FUNCTION main
                    RESULT before BOOLEAN
                    RESULT after BOOLEAN
                    RESULT deleted BOOLEAN
                BEGIN
                    CALL c0de/security/HAS_SECRET
                        PARAMETER name "pin" ;
                        RESULT result before
                    CALL c0de/security/SET_SECRET
                        PARAMETER name "pin" ;
                        PARAMETER value 1234 ;
                    CALL c0de/security/HAS_SECRET
                        PARAMETER name "pin" ;
                        RESULT result after
                    CALL c0de/security/DELETE_SECRET
                        PARAMETER name "pin" ;
                    CALL c0de/security/HAS_SECRET
                        PARAMETER name "pin" ;
                        RESULT result deleted
                END FUNCTION
                """, new Host(vault)));
        assertEquals(BooleanValue.FALSE, results.get("before"));
        assertEquals(BooleanValue.TRUE, results.get("after"));
        assertEquals(BooleanValue.FALSE, results.get("deleted"));
    }

    @Test
    void persistentSecretsNeedAnOpenVault() {
        String program = """
                INCLUDE c0de/security
                NOOP
                FUNCTION main
                BEGIN
                    %s
                    CALL c0de/security/SET_SECRET
                        PARAMETER name "pin" ;
                        PARAMETER value 1234 ;
                        PARAMETER persistent TRUE ;
                END FUNCTION
                """;
        MemoryVault closed = new MemoryVault().withPersistent("pw", false, List.of());
        crashed(run(program.formatted("NOOP"), new Host(closed)), ErrorCode.VAULT_NOT_OPEN, 7);

        MemoryVault vault = new MemoryVault().withPersistent("pw", false, List.of());
        String open = "CALL c0de/security/OPEN_VAULT\n        PARAMETER password \"pw\" ;";
        finished(run(program.formatted(open), new Host(vault)));
        assertEquals(new IntegerValue(1234), vault.stored().get("pin").value());

        MemoryVault wrong = new MemoryVault().withPersistent("pw", false, List.of());
        String wrongPassword = "CALL c0de/security/OPEN_VAULT\n        PARAMETER password \"no\" ;";
        crashed(run(program.formatted(wrongPassword), new Host(wrong)), ErrorCode.WRONG_PASSWORD, 6);
    }

    @Test
    void openingTheVaultAddsItsSecrets() {
        MemoryVault vault = new MemoryVault()
                .withPersistent("pw", false, List.of(new Vault.Secret("token", new StringValue("t"), true)));
        Map<String, Value> results = finished(run("""
                INCLUDE c0de/security
                NOOP
                FUNCTION main
                    RESULT exists BOOLEAN
                    RESULT open BOOLEAN
                    RESULT value STRING
                BEGIN
                    CALL c0de/security/HAS_VAULT
                        RESULT result exists
                    CALL c0de/security/OPEN_VAULT
                        PARAMETER password "pw" ;
                    CALL c0de/security/IS_VAULT_OPEN
                        RESULT result open
                    SECRET token STRING
                    ASSIGN value token ;
                END FUNCTION
                """, new Host(vault)));
        assertEquals(BooleanValue.TRUE, results.get("exists"));
        assertEquals(BooleanValue.TRUE, results.get("open"));
        assertEquals(string("t"), results.get("value"));
    }

    @Test
    void theCursorWaitsForPendingChanges() {
        // A vault whose changes take three steps, like a vault that waits for the client
        MemoryVault vault = new MemoryVault() {
            int waits = 3;

            @Override
            public Outcome set(Secret secret) {
                if (waits-- > 0) {
                    return Outcome.PENDING;
                }
                return super.set(secret);
            }
        };
        Cursor cursor = run("""
                INCLUDE c0de/security
                NOOP
                FUNCTION main
                BEGIN
                    CALL c0de/security/SET_SECRET
                        PARAMETER name "pin" ;
                        PARAMETER value 1 ;
                END FUNCTION
                """, new Host(vault));
        finished(cursor);
        assertTrue(vault.secret("pin").isPresent());
    }

    @Test
    void secretsAreNotSavedWithTheCursor() {
        String program = """
                FUNCTION main
                BEGIN
                    SECRET apiKey STRING
                    VARIABLE typed STRING
                    CALL INPUT
                        PARAMETER variable typed ;
                END FUNCTION
                """;
        // Without a console, INPUT waits forever
        Vault.Provider host = CursorSecurityTest::vaultWithKey;
        Cursor cursor = cursor(program, host);
        cursor.run(20);
        assertFalse(cursor.ended());
        Map<String, Object> data = CursorSnapshot.save(cursor);
        assertFalse(data.toString().contains("abc"), data.toString());
        Cursor restored = CursorSnapshot.restore(
                data, CursorTestSupport.program(program), CursorTestSupport.settings(StandardLibraries.registry()));
        restored.run(10);
        assertEquals(CursorState.CRASHED, restored.state());
        assertEquals(
                integer(ErrorCode.VAULT_UNAVAILABLE.code()), restored.error().get("code"));
    }

    @Test
    void inputHidesAndWriteHidesSensitiveValues() {
        Host host = new Host(new MemoryVault());
        finished(run("""
                FUNCTION main
                BEGIN
                    VARIABLE password STRING TAG(security: "sensitive")
                    VARIABLE name STRING
                    CALL INPUT
                        PARAMETER variable password ;
                        PARAMETER hide TRUE ;
                    CALL INPUT
                        PARAMETER variable name ;
                    CALL WRITE
                        PARAMETER value password ;
                    CALL WRITE
                        PARAMETER value name ;
                END FUNCTION
                """, host));
        assertEquals(
                List.of(
                        "input password hide=true",
                        "input name hide=false",
                        "write password=typed hidden=true",
                        "write name=typed hidden=false"),
                host.log);
    }
}
