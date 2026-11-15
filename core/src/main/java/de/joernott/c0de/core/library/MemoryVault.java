package de.joernott.c0de.core.library;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A vault in memory, for the program runner and tests (docs/concept/20-lib-security.md): the secrets of the vault
 * and, if the player has one, a persistent vault with its password and secrets. There is nobody to ask for a
 * password, so CREATE_VAULT and OPEN_VAULT without one raise VAULT_CANCELLED. Every operation is done at once.
 */
public class MemoryVault implements Vault {
    private final Map<String, Secret> secrets = new LinkedHashMap<>();
    private final Map<String, Secret> stored = new LinkedHashMap<>();
    private String password;
    private boolean open;

    /** A vault without secrets and without a persistent vault. */
    public MemoryVault() {}

    /** Adds a secret to the vault, e.g. from the options of the runner. */
    public MemoryVault withSecret(Secret secret) {
        secrets.put(secret.name(), secret);
        if (secret.persistent()) {
            stored.put(secret.name(), secret);
        }
        return this;
    }

    /**
     * Gives the player a persistent vault.
     *
     * @param vaultPassword its password
     * @param isOpen true if it is open already
     * @param storedSecrets the secrets in it; they are added to the vault when it is opened
     */
    public MemoryVault withPersistent(String vaultPassword, boolean isOpen, Iterable<Secret> storedSecrets) {
        this.password = vaultPassword;
        for (Secret secret : storedSecrets) {
            Secret persistent = new Secret(secret.name(), secret.value(), true);
            stored.put(secret.name(), persistent);
            if (isOpen) {
                secrets.put(secret.name(), persistent);
            }
        }
        this.open = isOpen;
        return this;
    }

    /** The secrets in the persistent vault, as a client would have written them. */
    public Map<String, Secret> stored() {
        return Map.copyOf(stored);
    }

    @Override
    public Optional<String> unavailable() {
        return Optional.empty();
    }

    @Override
    public Optional<Secret> secret(String name) {
        return Optional.ofNullable(secrets.get(name));
    }

    @Override
    public int size() {
        return secrets.size();
    }

    @Override
    public Outcome set(Secret secret) {
        Secret old = secrets.get(secret.name());
        boolean changesFile = secret.persistent() || (old != null && old.persistent());
        if (changesFile && !open) {
            throw C0deException.of(ErrorCode.VAULT_NOT_OPEN, "The persistent vault is not open");
        }
        secrets.put(secret.name(), secret);
        if (secret.persistent()) {
            stored.put(secret.name(), secret);
        } else {
            stored.remove(secret.name());
        }
        return Outcome.DONE;
    }

    @Override
    public Outcome delete(String name) {
        Secret old = secrets.get(name);
        if (old == null) {
            throw C0deException.of(ErrorCode.SECRET_NOT_FOUND, "The vault has no secret " + name);
        }
        if (old.persistent() && !open) {
            throw C0deException.of(ErrorCode.VAULT_NOT_OPEN, "The persistent vault is not open");
        }
        secrets.remove(name);
        stored.remove(name);
        return Outcome.DONE;
    }

    @Override
    public boolean exists() {
        return password != null;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public Outcome create(String newPassword) {
        if (exists()) {
            throw C0deException.of(ErrorCode.VAULT_ALREADY_EXISTS, "The player already has a persistent vault");
        }
        requirePassword(newPassword);
        password = newPassword;
        open = true;
        return Outcome.DONE;
    }

    @Override
    public Outcome open(String givenPassword) {
        if (!exists()) {
            throw C0deException.of(ErrorCode.VAULT_NOT_FOUND, "The player has no persistent vault");
        }
        if (open) {
            return Outcome.DONE;
        }
        requirePassword(givenPassword);
        if (!givenPassword.equals(password)) {
            throw C0deException.of(ErrorCode.WRONG_PASSWORD, "The password doesn't open the persistent vault");
        }
        open = true;
        secrets.putAll(stored);
        return Outcome.DONE;
    }

    private static void requirePassword(String password) {
        if (password.isEmpty()) {
            throw C0deException.of(ErrorCode.VAULT_CANCELLED, "There is nobody to ask for the password");
        }
    }
}
