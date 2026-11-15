package de.joernott.c0de.vault;

import de.soderer.utilities.kdbx.KdbxDatabase;
import de.soderer.utilities.kdbx.KdbxReader;
import de.soderer.utilities.kdbx.KdbxWriter;
import de.soderer.utilities.kdbx.data.KdbxEntry;
import de.soderer.utilities.kdbx.data.KdbxGroup;
import de.soderer.utilities.kdbx.data.KdbxHeaderFormat4;
import de.soderer.utilities.kdbx.data.KdbxTimes;
import de.soderer.utilities.kdbx.data.KeyDerivationFunctionInfoArgon;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The persistent vault of a player: a KeePass database (KDBX 4, Argon2id, AES-256) read and written with the
 * library kdbx (docs/concept/20-lib-security.md). Only the client of the player uses it; the server only stores
 * the encrypted bytes. The secrets are the entries of the group {@code c0de}: the title is the name, the password
 * field the value in the notation of a LITERAL block, and the field {@code c0de-type} the type.
 */
public final class VaultFile {
    /** The group of the secrets. */
    static final String GROUP = "c0de";
    /** The field of an entry with the type of the secret. */
    static final String TYPE_FIELD = "c0de-type";

    /** A secret as text: its name, type keyword and value. */
    public record Entry(String name, String type, String value) {}

    /** The password doesn't open the file. */
    public static final class WrongPasswordException extends Exception {
        WrongPasswordException(Throwable cause) {
            super("The password doesn't open the vault", cause);
        }
    }

    private final KdbxDatabase database;
    private final char[] password;

    private VaultFile(KdbxDatabase database, char[] password) {
        this.database = database;
        this.password = password.clone();
    }

    /** A new, empty vault with the password. */
    public static VaultFile create(char[] password) {
        KdbxDatabase database = new KdbxDatabase();
        database.getMeta().setDatabaseName("c0de");
        KdbxGroup group = new KdbxGroup();
        group.setName(GROUP);
        group.setTimes(times());
        database.getGroups().add(group);
        return new VaultFile(database, password);
    }

    /**
     * Decrypts a vault.
     *
     * @throws WrongPasswordException if the password doesn't open it
     * @throws Exception if the data is not a KDBX file or is damaged
     */
    public static VaultFile open(byte[] data, char[] password) throws Exception {
        KdbxDatabase database;
        try (KdbxReader reader = new KdbxReader(new ByteArrayInputStream(data))) {
            database = reader.readKdbxDatabase(password.clone());
        } catch (Exception e) {
            if (String.valueOf(e.getMessage()).contains("credentials")) {
                throw new WrongPasswordException(e);
            }
            throw e;
        }
        return new VaultFile(database, password);
    }

    /** The group of the secrets; it is created if a file edited elsewhere lost it. */
    private KdbxGroup group() {
        for (KdbxGroup group : database.getGroups()) {
            if (GROUP.equals(group.getName())) {
                return group;
            }
        }
        KdbxGroup group = new KdbxGroup();
        group.setName(GROUP);
        group.setTimes(times());
        database.getGroups().add(group);
        return group;
    }

    /** The secrets in the file. Entries without a type are left out. */
    public List<Entry> entries() {
        List<Entry> entries = new ArrayList<>();
        for (KdbxEntry entry : group().getEntries()) {
            String type = entry.getItem(TYPE_FIELD);
            if (entry.getTitle() != null && type != null) {
                String value = entry.getPassword();
                entries.add(new Entry(entry.getTitle(), type, value == null ? "" : value));
            }
        }
        return entries;
    }

    /** Creates or replaces a secret. */
    public void put(Entry secret) {
        remove(secret.name());
        KdbxEntry entry = new KdbxEntry();
        entry.setTitle(secret.name());
        entry.setPassword(secret.value());
        entry.setItem(TYPE_FIELD, secret.type());
        entry.setTimes(times());
        // KeePassXC reads the obfuscation of auto-type as a number, which kdbx leaves empty otherwise
        entry.setAutoType(false, "0", null, null, null);
        group().getEntries().add(entry);
    }

    /** Removes a secret; true if there was one. */
    public boolean remove(String name) {
        return group().getEntries().removeIf(entry -> name.equals(entry.getTitle()));
    }

    /** The encrypted file. */
    public byte[] bytes() throws Exception {
        KdbxHeaderFormat4 header = new KdbxHeaderFormat4();
        header.setKeyDerivationFunctionInfo(
                new KeyDerivationFunctionInfoArgon().withType(KeyDerivationFunctionInfoArgon.Argon2Type.Argon2_ID));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (KdbxWriter writer = new KdbxWriter(out)) {
            writer.writeKdbxDatabase(database, header, password.clone());
        }
        return out.toByteArray();
    }

    /** Times with every date set: KeePassXC refuses empty ones. */
    private static KdbxTimes times() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC).withNano(0);
        KdbxTimes times = new KdbxTimes();
        times.setCreationTime(now);
        times.setLastModificationTime(now);
        times.setLastAccessTime(now);
        times.setLocationChanged(now);
        times.setExpiryTime(now);
        times.setExpires(false);
        return times;
    }

    /** Forgets the password. */
    public void close() {
        Arrays.fill(password, '\0');
    }
}
