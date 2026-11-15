package de.joernott.c0de.vault;

import de.joernott.c0de.compat.Compat;
import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.library.SecretText;
import de.joernott.c0de.core.library.Vault;
import de.joernott.c0de.core.program.Names;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0de.dimension.C0deDimension;
import de.joernott.c0de.library.ProcessHost;
import de.joernott.c0de.network.VaultRequestPayload;
import de.joernott.c0de.network.VaultResponsePayload;
import de.joernott.c0de.platform.Platform;
import de.joernott.c0de.sheet.ProgramRegistry;
import de.joernott.c0de.sheet.SheetOwners;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

/**
 * The vaults of the players on the server (docs/concept/20-lib-security.md). Every player has a vault while they
 * are online: their secrets in memory, which vanish when they leave. The persistent vault is a KeePass file per
 * player in {@code <world>/data/c0de_vaults}, which the server stores but never decrypts: creating, opening and
 * changing it is done by the client of the player, which gets {@link VaultRequestPayload}s and answers with
 * {@link VaultResponsePayload}s. While the client works, the cursor waits on the CALL line.
 */
public final class VaultService {
    /** The folder of the persistent vaults in the world. */
    public static final String FOLDER = "data/c0de_vaults";

    private static final Map<UUID, PlayerVault> VAULTS = new ConcurrentHashMap<>();

    /**
     * Answers in place of a client, for GameTests whose mock players have no client: it gets the requests of all
     * players instead of their clients and answers them in the background.
     */
    private static volatile ClientStandIn standIn;

    private VaultService() {}

    /** Answers requests in place of the client of a player (GameTests). */
    @FunctionalInterface
    public interface ClientStandIn {
        VaultMessages.Answer answer(UUID player, String action, VaultMessages.Request request);
    }

    /** Registers the payloads and the events of the vaults. Called once when the mod is initialized. */
    public static void register() {
        Platform.INSTANCE.registerClientbound(VaultRequestPayload.TYPE, VaultRequestPayload.CODEC);
        Platform.INSTANCE.registerServerbound(VaultResponsePayload.TYPE, VaultResponsePayload.CODEC, VaultService::receive);
        Platform.INSTANCE.onPlayerJoin(player -> VAULTS.put(player.getUUID(), new PlayerVault(player.getUUID())));
        Platform.INSTANCE.onPlayerLeave(player -> forget(player.getUUID()));
    }

    /** Forgets the vault of a player, as when they leave the server. */
    public static void forget(UUID player) {
        VAULTS.remove(player);
    }

    /** Lets GameTests answer in place of clients; null ends it. */
    public static void standIn(ClientStandIn newStandIn) {
        standIn = newStandIn;
    }

    /** The file of the persistent vault of a player. */
    public static Path file(MinecraftServer server, UUID player) {
        return server.getWorldPath(LevelResource.ROOT).resolve(FOLDER).resolve(player + ".kdbx");
    }

    /**
     * The vault of a process: the vault of its activator, if that player is online and owns the sheet of the
     * program; otherwise a vault that raises VAULT_UNAVAILABLE.
     */
    public static Vault forProcess(ProcessHost host) {
        Optional<String> problem = problem(host);
        if (problem.isPresent()) {
            return unavailable(problem.get());
        }
        PlayerVault vault = VAULTS.computeIfAbsent(host.activator(), PlayerVault::new);
        return new ProcessVault(host.server(), vault, host.program() + "#" + host.pid());
    }

    private static Optional<String> problem(ProcessHost host) {
        if (host.activator() == null) {
            return Optional.of("The process has no vault: no player started it");
        }
        ServerPlayer player = host.server().getPlayerList().getPlayer(host.activator());
        if (player == null) {
            return Optional.of("The process has no vault: the player who started it is not online");
        }
        ServerLevel c0de = host.server().getLevel(C0deDimension.LEVEL);
        if (c0de == null) {
            return Optional.of("The process has no vault: the c0de dimension is missing");
        }
        OptionalInt sheet = ProgramRegistry.get(c0de).sheetOf(host.program());
        boolean owner = sheet.isPresent()
                && SheetOwners.get(c0de)
                        .owner(sheet.getAsInt())
                        .map(o -> o.id().equals(host.activator()))
                        .orElse(false);
        if (!owner) {
            return Optional.of("The process has no vault: the player who started it doesn't own the sheet");
        }
        return Optional.empty();
    }

    private static Vault unavailable(String reason) {
        return new Vault() {
            @Override
            public Optional<String> unavailable() {
                return Optional.of(reason);
            }

            private C0deException error() {
                return C0deException.of(ErrorCode.VAULT_UNAVAILABLE, reason);
            }

            @Override
            public Optional<Secret> secret(String name) {
                throw error();
            }

            @Override
            public int size() {
                throw error();
            }

            @Override
            public Outcome set(Secret secret) {
                throw error();
            }

            @Override
            public Outcome delete(String name) {
                throw error();
            }

            @Override
            public boolean exists() {
                throw error();
            }

            @Override
            public boolean isOpen() {
                throw error();
            }

            @Override
            public Outcome create(String password) {
                throw error();
            }

            @Override
            public Outcome open(String password) {
                throw error();
            }
        };
    }

    // ------------------------------------------------------------------ the answers of the clients

    private static void receive(VaultResponsePayload payload, ServerPlayer player) {
        PlayerVault vault = VAULTS.get(player.getUUID());
        if (vault != null) {
            vault.receive(Compat.server(player), payload);
        }
    }

    // ------------------------------------------------------------------ the vault of a player

    /** An operation on the persistent vault that waits for the client. */
    private static final class Operation {
        final String signature;
        final int request;
        Vault.Outcome outcome = Vault.Outcome.PENDING;
        C0deException error;
        Runnable onDone = () -> {};

        Operation(String signature, int request) {
            this.signature = signature;
            this.request = request;
        }
    }

    /** The vault of a player while they are online. */
    private static final class PlayerVault {
        final UUID player;
        final Map<String, Vault.Secret> secrets = new LinkedHashMap<>();
        boolean open;
        /** The operations by process. */
        final Map<String, Operation> pending = new HashMap<>();
        /** The answers that are still being received, by request. */
        final Map<Integer, ByteArrayOutputStream> answers = new HashMap<>();
        int nextRequest = 1;

        PlayerVault(UUID player) {
            this.player = player;
        }

        Operation operation(int request) {
            for (Operation operation : pending.values()) {
                if (operation.request == request) {
                    return operation;
                }
            }
            return null;
        }

        void receive(MinecraftServer server, VaultResponsePayload payload) {
            Operation operation = operation(payload.request());
            if (operation == null || operation.outcome != Vault.Outcome.PENDING) {
                answers.remove(payload.request());
                return;
            }
            ByteArrayOutputStream answer = answers.computeIfAbsent(payload.request(), r -> new ByteArrayOutputStream());
            answer.writeBytes(payload.chunk());
            if (answer.size() > VaultMessages.MAX_LENGTH) {
                answers.remove(payload.request());
                fail(operation, ErrorCode.VAULT_FAILED, "The answer of the client is too long");
                return;
            }
            if (!payload.last()) {
                return;
            }
            answers.remove(payload.request());
            VaultMessages.Answer result;
            try {
                result = VaultMessages.Answer.read(answer.toByteArray());
            } catch (IllegalArgumentException e) {
                fail(operation, ErrorCode.VAULT_FAILED, e.getMessage());
                return;
            }
            answered(server, operation, result);
        }

        /** Applies the answer of the client to a waiting operation. */
        void answered(MinecraftServer server, Operation operation, VaultMessages.Answer answer) {
            switch (answer.status()) {
                case VaultMessages.OK -> {
                    try {
                        if (answer.file().length > 0) {
                            store(server, answer.file());
                        }
                        operation.onDone.run();
                        if (!answer.entries().isEmpty()) {
                            addSecrets(answer.entries());
                        }
                        operation.outcome = Vault.Outcome.DONE;
                    } catch (IOException | IllegalArgumentException e) {
                        fail(operation, ErrorCode.VAULT_FAILED, "The vault can't be stored: " + e.getMessage());
                    }
                }
                case VaultMessages.CANCELLED ->
                    fail(operation, ErrorCode.VAULT_CANCELLED, "The player closed the password dialog");
                case VaultMessages.WRONG_PASSWORD ->
                    fail(operation, ErrorCode.WRONG_PASSWORD, "The password doesn't open the persistent vault");
                default -> fail(operation, ErrorCode.VAULT_FAILED, "The client failed: " + answer.message());
            }
        }

        void fail(Operation operation, ErrorCode code, String message) {
            operation.error = C0deException.of(code, message);
            operation.outcome = Vault.Outcome.DONE;
        }

        /** The secrets of the opened persistent vault join the vault. */
        void addSecrets(List<VaultFile.Entry> entries) {
            if (entries.size() > Vault.MAX_SECRETS) {
                throw new IllegalArgumentException("The vault holds more than " + Vault.MAX_SECRETS + " secrets");
            }
            Map<String, Vault.Secret> read = new LinkedHashMap<>();
            for (VaultFile.Entry entry : entries) {
                if (!Names.isValid(entry.name())) {
                    throw new IllegalArgumentException("Not a valid name of a secret: " + entry.name());
                }
                Value value = SecretText.parse(entry.type(), entry.value());
                read.put(entry.name(), new Vault.Secret(entry.name(), value, true));
            }
            secrets.putAll(read);
        }

        void store(MinecraftServer server, byte[] file) throws IOException {
            if (file.length > VaultMessages.MAX_LENGTH) {
                throw new IOException("The file is too large");
            }
            Path target = file(server, player);
            Files.createDirectories(target.getParent());
            Path temporary = target.resolveSibling(player + ".kdbx.tmp");
            Files.write(temporary, file);
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        }

        /**
         * Starts an operation for a process or returns the state of the one it started. The cursor calls the
         * function again in every step with the same arguments, so the same signature means the same operation.
         */
        Vault.Outcome call(
                MinecraftServer server,
                String process,
                String signature,
                String action,
                VaultMessages.Request request,
                Runnable onDone) {
            Operation operation = pending.get(process);
            if (operation != null && operation.signature.equals(signature)) {
                if (operation.outcome == Vault.Outcome.PENDING) {
                    return Vault.Outcome.PENDING;
                }
                pending.remove(process);
                if (operation.error != null) {
                    throw operation.error;
                }
                return Vault.Outcome.DONE;
            }
            operation = new Operation(signature, nextRequest++);
            operation.onDone = onDone;
            pending.put(process, operation);
            ClientStandIn current = standIn;
            if (current != null) {
                // Like a client, the stand-in encrypts in the background and answers in a later tick
                Operation started = operation;
                CompletableFuture.supplyAsync(() -> current.answer(player, action, request))
                        .thenAccept(answer -> server.execute(() -> {
                            if (started.outcome == Vault.Outcome.PENDING) {
                                answered(server, started, answer);
                            }
                        }));
            } else {
                ServerPlayer target = server.getPlayerList().getPlayer(player);
                if (target == null) {
                    pending.remove(process);
                    throw C0deException.of(ErrorCode.VAULT_UNAVAILABLE, "The player is not online");
                }
                Platform.INSTANCE.sendToPlayer(
                        target, new VaultRequestPayload(operation.request, action, request.bytes()));
            }
            return call(server, process, signature, action, request, onDone);
        }
    }

    /** The view of a process on the vault of its activator. */
    private record ProcessVault(MinecraftServer server, PlayerVault vault, String process) implements Vault {
        @Override
        public Optional<String> unavailable() {
            return Optional.empty();
        }

        @Override
        public Optional<Secret> secret(String name) {
            return Optional.ofNullable(vault.secrets.get(name));
        }

        @Override
        public int size() {
            return vault.secrets.size();
        }

        @Override
        public boolean exists() {
            return Files.exists(file(server, vault.player));
        }

        @Override
        public boolean isOpen() {
            return vault.open;
        }

        private void requireOpen() {
            if (!vault.open) {
                throw C0deException.of(ErrorCode.VAULT_NOT_OPEN, "The persistent vault is not open");
            }
        }

        @Override
        public Outcome set(Secret secret) {
            Secret old = vault.secrets.get(secret.name());
            if (!secret.persistent() && (old == null || !old.persistent())) {
                vault.secrets.put(secret.name(), secret);
                return Outcome.DONE;
            }
            requireOpen();
            String type = SecretText.type(secret.value());
            String text = SecretText.text(secret.value());
            if (secret.persistent()) {
                return vault.call(
                        server,
                        process,
                        "put " + secret.name() + " " + type + " " + text.hashCode(),
                        "put",
                        new VaultMessages.Request(List.of(secret.name(), type, text), new byte[0]),
                        () -> vault.secrets.put(secret.name(), secret));
            }
            // A persistent secret that becomes a secret of the vault only leaves the file
            return vault.call(
                    server,
                    process,
                    "unpersist " + secret.name() + " " + type + " " + text.hashCode(),
                    "remove",
                    new VaultMessages.Request(List.of(secret.name()), new byte[0]),
                    () -> vault.secrets.put(secret.name(), secret));
        }

        @Override
        public Outcome delete(String name) {
            Secret old = vault.secrets.get(name);
            if (old == null) {
                throw C0deException.of(ErrorCode.SECRET_NOT_FOUND, "The vault has no secret " + name);
            }
            if (!old.persistent()) {
                vault.secrets.remove(name);
                return Outcome.DONE;
            }
            requireOpen();
            return vault.call(
                    server,
                    process,
                    "remove " + name,
                    "remove",
                    new VaultMessages.Request(List.of(name), new byte[0]),
                    () -> vault.secrets.remove(name));
        }

        @Override
        public Outcome create(String password) {
            Operation running = vault.pending.get(process);
            if (running == null && exists()) {
                throw C0deException.of(ErrorCode.VAULT_ALREADY_EXISTS, "The player already has a persistent vault");
            }
            return vault.call(
                    server,
                    process,
                    "create",
                    "create",
                    new VaultMessages.Request(List.of(password), new byte[0]),
                    () -> vault.open = true);
        }

        @Override
        public Outcome open(String password) {
            Operation running = vault.pending.get(process);
            if (running == null) {
                if (!exists()) {
                    throw C0deException.of(ErrorCode.VAULT_NOT_FOUND, "The player has no persistent vault");
                }
                if (vault.open) {
                    return Outcome.DONE;
                }
            }
            byte[] file;
            try {
                file = running == null ? Files.readAllBytes(file(server, vault.player)) : new byte[0];
            } catch (IOException e) {
                throw C0deException.of(ErrorCode.VAULT_FAILED, "The persistent vault can't be read: " + e.getMessage());
            }
            return vault.call(
                    server,
                    process,
                    "open",
                    "open",
                    new VaultMessages.Request(List.of(password), file),
                    () -> vault.open = true);
        }
    }

}
