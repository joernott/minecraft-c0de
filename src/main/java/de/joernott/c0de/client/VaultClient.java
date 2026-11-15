package de.joernott.c0de.client;

import de.joernott.c0de.network.VaultRequestPayload;
import de.joernott.c0de.network.VaultResponsePayload;
import de.joernott.c0de.platform.Platform;
import de.joernott.c0de.vault.VaultFile;
import de.joernott.c0de.vault.VaultMessages;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;

/**
 * The client side of the persistent vault (docs/concept/20-lib-security.md): it creates, decrypts and changes the
 * KeePass file of the player when the server asks for it. The decrypted database and its password stay in the
 * memory of the client while the player is on the server; the server only gets the encrypted file and, when the
 * vault is opened, the secrets. Encrypting takes a moment (Argon2), so it runs in the background.
 */
public final class VaultClient {
    /** The open vault, or null. */
    private static VaultFile open;
    /** The connection the open vault belongs to; another server or a new session forgets it. */
    private static Object connection;

    private VaultClient() {}

    /** A request of the server. Called on the client thread. */
    public static void receive(VaultRequestPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (connection != minecraft.getConnection()) {
            forget();
            connection = minecraft.getConnection();
        }
        VaultMessages.Request request;
        try {
            request = VaultMessages.Request.read(payload.data());
        } catch (IllegalArgumentException e) {
            answer(payload.request(), VaultMessages.Answer.of(VaultMessages.FAILED, e.getMessage()));
            return;
        }
        int number = payload.request();
        switch (payload.action()) {
            case "create" -> withPassword(number, request.string(0), true, password -> create(number, password));
            case "open" -> open(number, request);
            case "put" -> change(number, file -> file.put(
                    new VaultFile.Entry(request.string(0), request.string(1), request.string(2))));
            case "remove" -> change(number, file -> file.remove(request.string(0)));
            default -> answer(number, VaultMessages.Answer.of(VaultMessages.FAILED, "Unknown request " + payload.action()));
        }
    }

    /** Forgets the open vault, e.g. when the player leaves the server. */
    public static void forget() {
        if (open != null) {
            open.close();
            open = null;
        }
    }

    /** Runs the action with the password of the request, or asks the player for one. */
    private static void withPassword(int request, String password, boolean create, Consumer<char[]> action) {
        if (!password.isEmpty()) {
            action.accept(password.toCharArray());
            return;
        }
        PasswordScreen.ask(create, action, () -> answer(request, VaultMessages.Answer.of(VaultMessages.CANCELLED, "")));
    }

    private static void create(int request, char[] password) {
        CompletableFuture.supplyAsync(() -> {
                    try {
                        VaultFile file = VaultFile.create(password);
                        return new Created(file, file.bytes());
                    } catch (Exception e) {
                        return new Created(null, null);
                    }
                }, VaultFile.EXECUTOR)
                .thenAcceptAsync(
                        created -> {
                            if (created.file() == null) {
                                answer(request, VaultMessages.Answer.of(VaultMessages.FAILED, "The vault can't be created"));
                                return;
                            }
                            forget();
                            open = created.file();
                            answer(request, VaultMessages.Answer.ok(created.bytes(), List.of()));
                        },
                        Minecraft.getInstance());
    }

    private record Created(VaultFile file, byte[] bytes) {}

    private static void open(int request, VaultMessages.Request arguments) {
        byte[] data = arguments.file();
        String given = arguments.string(0);
        if (!given.isEmpty()) {
            decrypt(data, given.toCharArray(), file -> opened(request, file), wrong -> answer(
                    request, VaultMessages.Answer.of(VaultMessages.WRONG_PASSWORD, "")), failure -> answer(
                    request, VaultMessages.Answer.of(VaultMessages.FAILED, failure)));
            return;
        }
        // The dialog stays open after a wrong password for another try
        PasswordScreen.ask(
                false,
                password -> decrypt(
                        data,
                        password,
                        file -> {
                            PasswordScreen.done();
                            opened(request, file);
                        },
                        wrong -> PasswordScreen.wrongPassword(),
                        failure -> {
                            PasswordScreen.done();
                            answer(request, VaultMessages.Answer.of(VaultMessages.FAILED, failure));
                        }),
                () -> answer(request, VaultMessages.Answer.of(VaultMessages.CANCELLED, "")));
    }

    private static void decrypt(
            byte[] data, char[] password, Consumer<VaultFile> onOpen, Consumer<String> onWrong, Consumer<String> onFailure) {
        CompletableFuture.supplyAsync(() -> {
                    try {
                        return (Object) VaultFile.open(data, password);
                    } catch (VaultFile.WrongPasswordException e) {
                        return Boolean.FALSE;
                    } catch (Exception e) {
                        return String.valueOf(e.getMessage());
                    }
                }, VaultFile.EXECUTOR)
                .thenAcceptAsync(
                        result -> {
                            if (result instanceof VaultFile file) {
                                onOpen.accept(file);
                            } else if (result instanceof String failure) {
                                onFailure.accept(failure);
                            } else {
                                onWrong.accept("");
                            }
                        },
                        Minecraft.getInstance());
    }

    private static void opened(int request, VaultFile file) {
        forget();
        open = file;
        answer(request, VaultMessages.Answer.ok(new byte[0], file.entries()));
    }

    /** A change of a secret: the file is encrypted again and sent to the server. */
    private static void change(int request, Consumer<VaultFile> change) {
        VaultFile file = open;
        if (file == null) {
            answer(request, VaultMessages.Answer.of(VaultMessages.FAILED, "The vault is not open on this client"));
            return;
        }
        change.accept(file);
        CompletableFuture.supplyAsync(() -> {
                    try {
                        return file.bytes();
                    } catch (Exception e) {
                        return null;
                    }
                }, VaultFile.EXECUTOR)
                .thenAcceptAsync(
                        bytes -> answer(
                                request,
                                bytes == null
                                        ? VaultMessages.Answer.of(VaultMessages.FAILED, "The vault can't be written")
                                        : VaultMessages.Answer.ok(bytes, List.of())),
                        Minecraft.getInstance());
    }

    private static void answer(int request, VaultMessages.Answer answer) {
        for (VaultResponsePayload part : answer.payloads(request)) {
            Platform.INSTANCE.sendToServer(part);
        }
    }
}
