package de.joernott.c0de.core.library.standard;

import static de.joernott.c0de.core.library.standard.Functions.BOOLEAN;
import static de.joernott.c0de.core.library.standard.Functions.invalid;
import static de.joernott.c0de.core.library.standard.Functions.optional;
import static de.joernott.c0de.core.library.standard.Functions.required;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.library.Library;
import de.joernott.c0de.core.library.NativeCall;
import de.joernott.c0de.core.library.NativeFunction;
import de.joernott.c0de.core.library.NativeResult;
import de.joernott.c0de.core.library.SecretText;
import de.joernott.c0de.core.library.Vault;
import de.joernott.c0de.core.program.Names;
import de.joernott.c0de.core.value.BooleanValue;
import de.joernott.c0de.core.value.CharValue;
import de.joernott.c0de.core.value.FloatValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.TimeValue;
import de.joernott.c0de.core.value.Value;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * The library {@code c0de/security} (docs/concept/20-lib-security.md): the secrets of the vault of the process.
 * The vault itself is implemented by the mod and the program runner ({@link Vault}).
 */
public final class SecurityLibrary implements Library {
    public static final String NAME = "c0de/security";

    /** The types of secrets: the simple types and STRING. */
    static final Predicate<Value> SECRET_VALUE = value -> value instanceof BooleanValue
            || value instanceof IntegerValue
            || value instanceof FloatValue
            || value instanceof CharValue
            || value instanceof StringValue
            || value instanceof TimeValue;

    private static final String SECRET_TYPES = "BOOLEAN|INTEGER|FLOAT|CHAR|STRING|TIME";
    private static final Predicate<Value> STRING = value -> value instanceof StringValue;
    private static final NativeFunction.Parameter NAME_PARAMETER = required("name", "STRING", STRING);
    private static final NativeFunction.Parameter PASSWORD_PARAMETER =
            optional("password", "STRING", STRING, new StringValue(""));

    private static final Map<String, NativeFunction> FUNCTIONS = new Functions.Builder()
            .add(
                    "SET_SECRET",
                    List.of(
                            NAME_PARAMETER,
                            required("value", SECRET_TYPES, SECRET_VALUE),
                            optional("persistent", "BOOLEAN", BOOLEAN, BooleanValue.FALSE)),
                    List.of(),
                    call -> {
                        Vault vault = vault(call);
                        String name = name(call);
                        if (vault.secret(name).isEmpty() && vault.size() >= Vault.MAX_SECRETS) {
                            throw invalid("A vault holds at most " + Vault.MAX_SECRETS + " secrets");
                        }
                        Value value = call.argument("value");
                        if (value.text().length() > SecretText.MAX_LENGTH) {
                            throw invalid("A secret has at most " + SecretText.MAX_LENGTH + " characters");
                        }
                        boolean persistent = ((BooleanValue) call.argument("persistent")).value();
                        return outcome(vault.set(new Vault.Secret(name, value.copy(), persistent)));
                    })
            .add("DELETE_SECRET", List.of(NAME_PARAMETER), List.of(), call -> {
                Vault vault = vault(call);
                String name = name(call);
                if (vault.secret(name).isEmpty()) {
                    throw C0deException.of(ErrorCode.SECRET_NOT_FOUND, "The vault has no secret " + name);
                }
                return outcome(vault.delete(name));
            })
            .add(
                    "HAS_SECRET",
                    List.of(NAME_PARAMETER),
                    call -> BooleanValue.of(vault(call).secret(name(call)).isPresent()))
            .add("CREATE_VAULT", List.of(PASSWORD_PARAMETER), List.of(), call -> {
                Vault vault = vault(call);
                return outcome(vault.create(((StringValue) call.argument("password")).value()));
            })
            .add("OPEN_VAULT", List.of(PASSWORD_PARAMETER), List.of(), call -> {
                Vault vault = vault(call);
                return outcome(vault.open(((StringValue) call.argument("password")).value()));
            })
            .add("HAS_VAULT", List.of(), call -> BooleanValue.of(vault(call).exists()))
            .add("IS_VAULT_OPEN", List.of(), call -> BooleanValue.of(vault(call).isOpen()))
            .build();

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Map<String, NativeFunction> functions() {
        return FUNCTIONS;
    }

    /** The vault of the process; VAULT_UNAVAILABLE if it has none. */
    private static Vault vault(NativeCall call) {
        Vault vault = call.process().vault();
        vault.unavailable().ifPresent(reason -> {
            throw C0deException.of(ErrorCode.VAULT_UNAVAILABLE, reason);
        });
        return vault;
    }

    /** The parameter {@code name}, which must be a valid name. */
    private static String name(NativeCall call) {
        String name = ((StringValue) call.argument("name")).value();
        if (!Names.isValid(name)) {
            throw invalid("Not a valid name of a secret: " + name);
        }
        return name;
    }

    private static NativeResult outcome(Vault.Outcome outcome) {
        return outcome == Vault.Outcome.PENDING ? NativeResult.waitForValue() : NativeResult.none();
    }
}
