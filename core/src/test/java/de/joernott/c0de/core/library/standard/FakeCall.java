package de.joernott.c0de.core.library.standard;

import de.joernott.c0de.core.cursor.C0deRandom;
import de.joernott.c0de.core.eval.Variable;
import de.joernott.c0de.core.library.Library;
import de.joernott.c0de.core.library.NativeCall;
import de.joernott.c0de.core.library.NativeFunction;
import de.joernott.c0de.core.library.NativeResult;
import de.joernott.c0de.core.library.ProcessContext;
import de.joernott.c0de.core.value.Value;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.random.RandomGenerator;

/** Calls native functions directly, without a cursor. The parameters are not checked. */
final class FakeCall implements NativeCall, ProcessContext {
    /** 2026-10-19T12:05:09.250Z, a Monday (14:05:09.250 in Berlin). */
    static final long NOW = 1792411509250L;

    static final ZoneId BERLIN = ZoneId.of("Europe/Berlin");

    private final NativeFunction function;
    private final Map<String, Value> arguments = new HashMap<>();
    private final Map<String, Map<String, String>> tags = new HashMap<>();
    private final Map<String, Variable> references = new HashMap<>();
    private C0deRandom random = new C0deRandom(1);

    private FakeCall(NativeFunction function) {
        this.function = function;
    }

    static FakeCall of(Library library, String function) {
        NativeFunction nativeFunction = library.functions().get(function);
        if (nativeFunction == null) {
            throw new IllegalArgumentException(library.name() + " has no function " + function);
        }
        return new FakeCall(nativeFunction);
    }

    FakeCall with(String name, Value value) {
        arguments.put(name, value);
        return this;
    }

    FakeCall tags(String name, Map<String, String> parameterTags) {
        tags.put(name, parameterTags);
        return this;
    }

    FakeCall reference(String name, Variable variable) {
        references.put(name, variable);
        arguments.put(name, variable.value());
        return this;
    }

    NativeResult call() {
        return function.implementation().call(this);
    }

    /** The result named {@code result}. */
    Value result() {
        return call().results().get("result");
    }

    @Override
    public Value argument(String name) {
        Value value = arguments.get(name);
        return value != null
                ? value
                : function.parameter(name)
                        .map(NativeFunction.Parameter::defaultValue)
                        .orElse(null);
    }

    @Override
    public boolean provided(String name) {
        return arguments.containsKey(name);
    }

    @Override
    public Map<String, String> tags(String name) {
        return tags.getOrDefault(name, Map.of());
    }

    @Override
    public Optional<Variable> reference(String name) {
        return Optional.ofNullable(references.get(name));
    }

    @Override
    public ProcessContext process() {
        return this;
    }

    @Override
    public RandomGenerator random() {
        return random;
    }

    @Override
    public void randomize(long seed) {
        random = new C0deRandom(seed);
    }

    @Override
    public Clock clock() {
        return Clock.fixed(Instant.ofEpochMilli(NOW), ZoneOffset.UTC);
    }

    @Override
    public ZoneId zone() {
        return BERLIN;
    }

    @Override
    public Object host() {
        return null;
    }
}
