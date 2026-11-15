package de.joernott.c0de.vault;

import de.joernott.c0de.network.VaultResponsePayload;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The contents of the vault payloads (docs/concept/20-lib-security.md): the arguments of a request and the answer
 * of the client, as bytes that look the same on all Minecraft versions.
 */
public final class VaultMessages {
    /** The client did what was asked. */
    public static final String OK = "ok";
    /** The player closed the password dialog. */
    public static final String CANCELLED = "cancelled";
    /** The password given by the program doesn't open the vault. */
    public static final String WRONG_PASSWORD = "wrong_password";
    /** The client couldn't read or write the file. */
    public static final String FAILED = "failed";

    /** The largest answer and the largest file the server accepts, in bytes. */
    public static final int MAX_LENGTH = 4 * 1024 * 1024;

    private VaultMessages() {}

    /** The arguments of a request: strings and, for {@code open}, the file. */
    public record Request(List<String> strings, byte[] file) {
        public byte[] bytes() {
            return write(out -> {
                out.writeInt(strings.size());
                for (String string : strings) {
                    writeString(out, string);
                }
                writeBytes(out, file);
            });
        }

        public static Request read(byte[] data) {
            return VaultMessages.read(data, in -> {
                int count = in.readInt();
                if (count < 0 || count > 16) {
                    throw new IOException("Too many arguments: " + count);
                }
                List<String> strings = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    strings.add(readString(in));
                }
                return new Request(strings, readBytes(in));
            });
        }

        /** The argument at the index, or the empty string. */
        public String string(int index) {
            return index < strings.size() ? strings.get(index) : "";
        }
    }

    /**
     * The answer of the client.
     *
     * @param status {@link #OK}, {@link #CANCELLED}, {@link #WRONG_PASSWORD} or {@link #FAILED}
     * @param message why it failed
     * @param file the new encrypted file (create, put, remove), or empty
     * @param entries the secrets of the file (open)
     */
    public record Answer(String status, String message, byte[] file, List<VaultFile.Entry> entries) {
        public static Answer ok(byte[] file, List<VaultFile.Entry> entries) {
            return new Answer(OK, "", file, entries);
        }

        public static Answer of(String status, String message) {
            return new Answer(status, message, new byte[0], List.of());
        }

        public byte[] bytes() {
            return write(out -> {
                writeString(out, status);
                writeString(out, message);
                writeBytes(out, file);
                out.writeInt(entries.size());
                for (VaultFile.Entry entry : entries) {
                    writeString(out, entry.name());
                    writeString(out, entry.type());
                    writeString(out, entry.value());
                }
            });
        }

        public static Answer read(byte[] data) {
            return VaultMessages.read(data, in -> {
                String status = readString(in);
                String message = readString(in);
                byte[] file = readBytes(in);
                int count = in.readInt();
                if (count < 0 || count > 10_000) {
                    throw new IOException("Too many secrets: " + count);
                }
                List<VaultFile.Entry> entries = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    entries.add(new VaultFile.Entry(readString(in), readString(in), readString(in)));
                }
                return new Answer(status, message, file, entries);
            });
        }

        /** The answer in parts for {@link VaultResponsePayload}s. */
        public List<VaultResponsePayload> payloads(int request) {
            byte[] data = bytes();
            List<VaultResponsePayload> payloads = new ArrayList<>();
            int length = VaultResponsePayload.CHUNK_LENGTH;
            int start = 0;
            do {
                int end = Math.min(data.length, start + length);
                payloads.add(
                        new VaultResponsePayload(request, end == data.length, Arrays.copyOfRange(data, start, end)));
                start = end;
            } while (start < data.length);
            return payloads;
        }
    }

    private interface Writer {
        void write(DataOutputStream out) throws IOException;
    }

    private interface Reader<T> {
        T read(DataInputStream in) throws IOException;
    }

    private static byte[] write(Writer writer) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes)) {
            writer.write(out);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bytes.toByteArray();
    }

    private static <T> T read(byte[] data, Reader<T> reader) {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(data))) {
            return reader.read(in);
        } catch (IOException e) {
            throw new IllegalArgumentException("A damaged vault message: " + e.getMessage(), e);
        }
    }

    private static void writeString(DataOutputStream out, String text) throws IOException {
        writeBytes(out, text.getBytes(StandardCharsets.UTF_8));
    }

    private static String readString(DataInputStream in) throws IOException {
        return new String(readBytes(in), StandardCharsets.UTF_8);
    }

    private static void writeBytes(DataOutputStream out, byte[] data) throws IOException {
        out.writeInt(data.length);
        out.write(data);
    }

    private static byte[] readBytes(DataInputStream in) throws IOException {
        int length = in.readInt();
        if (length < 0 || length > MAX_LENGTH) {
            throw new IOException("A part is too long: " + length);
        }
        byte[] data = new byte[length];
        in.readFully(data);
        return data;
    }
}
