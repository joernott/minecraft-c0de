package de.joernott.c0de.core.library;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.value.Value;
import java.util.Optional;

/**
 * The vault of secrets a process uses: the vault of its activator (docs/concept/20-lib-security.md). The mod and
 * the program runner implement it; {@link #NONE} is the vault of a process without one.
 *
 * <p>Changes of the persistent vault need the client of the player, so they take several steps: such a method
 * returns {@link Outcome#PENDING}, and the cursor calls it again with the same arguments in the next step until
 * it returns {@link Outcome#DONE} or throws. The implementation keeps the pending operation of the process.
 */
public interface Vault {
    /** The vault of a process that has none: every access raises VAULT_UNAVAILABLE. */
    Vault NONE = new Vault() {
        @Override
        public Optional<String> unavailable() {
            return Optional.of("The process has no vault: it was not started by the owner of the sheet");
        }

        @Override
        public Optional<Secret> secret(String name) {
            throw unavailableError();
        }

        @Override
        public int size() {
            throw unavailableError();
        }

        @Override
        public Outcome set(Secret secret) {
            throw unavailableError();
        }

        @Override
        public Outcome delete(String name) {
            throw unavailableError();
        }

        @Override
        public boolean exists() {
            throw unavailableError();
        }

        @Override
        public boolean isOpen() {
            throw unavailableError();
        }

        @Override
        public Outcome create(String password) {
            throw unavailableError();
        }

        @Override
        public Outcome open(String password) {
            throw unavailableError();
        }

        private C0deException unavailableError() {
            return C0deException.of(ErrorCode.VAULT_UNAVAILABLE, unavailable().orElseThrow());
        }
    };

    /** The most secrets a vault holds. */
    int MAX_SECRETS = 256;

    /** A secret: a name, a value (whose type is the type of the secret) and whether it is persistent. */
    record Secret(String name, Value value, boolean persistent) {}

    /** The state of an operation that needs the client. */
    enum Outcome {
        DONE,
        PENDING
    }

    /** Something that gives a process its vault: the host of the mod, or of the program runner. */
    interface Provider {
        Vault vault();
    }

    /** Why the process can't use the vault, or empty if it can. */
    Optional<String> unavailable();

    /** The secret with this name. */
    Optional<Secret> secret(String name);

    /** The number of secrets. */
    int size();

    /**
     * Creates or replaces a secret. A persistent secret, or one that replaces a persistent secret, changes the
     * persistent vault, which must be open (VAULT_NOT_OPEN).
     */
    Outcome set(Secret secret);

    /** Removes a secret (SECRET_NOT_FOUND if there is none); a persistent one also from the persistent vault. */
    Outcome delete(String name);

    /** True if the player has a persistent vault. */
    boolean exists();

    /** True if the persistent vault is open. */
    boolean isOpen();

    /**
     * Creates the persistent vault and opens it (VAULT_ALREADY_EXISTS, VAULT_CANCELLED).
     *
     * @param password the password, or the empty string to ask the player
     */
    Outcome create(String password);

    /**
     * Opens the persistent vault and adds its secrets (VAULT_NOT_FOUND, WRONG_PASSWORD, VAULT_CANCELLED).
     *
     * @param password the password, or the empty string to ask the player
     */
    Outcome open(String password);
}
