package de.joernott.c0de.core.library.standard;

import static de.joernott.c0de.core.library.standard.Functions.INTEGER;
import static de.joernott.c0de.core.library.standard.Functions.NUMBER;
import static de.joernott.c0de.core.library.standard.Functions.integer;
import static de.joernott.c0de.core.library.standard.Functions.invalid;
import static de.joernott.c0de.core.library.standard.Functions.number;
import static de.joernott.c0de.core.library.standard.Functions.overflow;
import static de.joernott.c0de.core.library.standard.Functions.required;
import static de.joernott.c0de.core.library.standard.Functions.toLong;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.library.Library;
import de.joernott.c0de.core.library.NativeCall;
import de.joernott.c0de.core.library.NativeFunction;
import de.joernott.c0de.core.library.NativeResult;
import de.joernott.c0de.core.value.FloatValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.Value;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

/**
 * The library {@code c0de/math} (docs/concept/09-lib-math.md). Functions documented with a FLOAT parameter
 * also accept INTEGERs, as program functions do.
 */
public final class MathLibrary implements Library {
    public static final String NAME = "c0de/math";

    private static final List<NativeFunction.Parameter> NUMBER_VALUE =
            List.of(required("value", "INTEGER|FLOAT", NUMBER));

    private static final Map<String, NativeFunction> FUNCTIONS = new Functions.Builder()
            .add("ABS", NUMBER_VALUE, MathLibrary::abs)
            .add("SGN", NUMBER_VALUE, call -> new IntegerValue((long) Math.signum(number(call, "value"))))
            .add("SQR", NUMBER_VALUE, call -> {
                double value = number(call, "value");
                if (value < 0) {
                    throw invalid("The square root of a negative number is not defined: " + value);
                }
                return new FloatValue(Math.sqrt(value));
            })
            .add("EXP", NUMBER_VALUE, floats(Math::exp))
            .add("LOG", NUMBER_VALUE, call -> {
                double value = number(call, "value");
                if (value <= 0) {
                    throw invalid("The logarithm is only defined for numbers greater than 0: " + value);
                }
                return new FloatValue(Math.log(value));
            })
            .add("SIN", NUMBER_VALUE, floats(Math::sin))
            .add("COS", NUMBER_VALUE, floats(Math::cos))
            .add("TAN", NUMBER_VALUE, floats(Math::tan))
            .add("ATN", NUMBER_VALUE, floats(Math::atan))
            .add("INT", NUMBER_VALUE, call -> new IntegerValue(toLong(Math.floor(number(call, "value")))))
            .add("FIX", NUMBER_VALUE, call -> new IntegerValue(toLong(number(call, "value"))))
            .add("ROUND", NUMBER_VALUE, call -> new IntegerValue(toLong(Math.rint(number(call, "value")))))
            .add("TO_FLOAT", NUMBER_VALUE, call -> new FloatValue(number(call, "value")))
            .add(
                    "DIV",
                    List.of(required("dividend", "INTEGER", INTEGER), required("divisor", "INTEGER", INTEGER)),
                    MathLibrary::div)
            .add(
                    "RANDOM",
                    List.of(),
                    call -> new FloatValue(call.process().random().nextDouble()))
            .add("RANDOMIZE", List.of(required("seed", "INTEGER", INTEGER)), List.of(), call -> {
                call.process().randomize(integer(call, "seed"));
                return NativeResult.none();
            })
            .build();

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Map<String, Value> constants() {
        return Map.of("PI", new FloatValue(Math.PI));
    }

    @Override
    public Map<String, NativeFunction> functions() {
        return FUNCTIONS;
    }

    private static Functions.ResultFunction floats(DoubleUnaryOperator operation) {
        return call -> new FloatValue(operation.applyAsDouble(number(call, "value")));
    }

    private static Value abs(NativeCall call) {
        if (call.argument("value") instanceof IntegerValue integer) {
            if (integer.value() == Long.MIN_VALUE) {
                throw overflow();
            }
            return new IntegerValue(Math.abs(integer.value()));
        }
        return new FloatValue(Math.abs(number(call, "value")));
    }

    private static Value div(NativeCall call) {
        long dividend = integer(call, "dividend");
        long divisor = integer(call, "divisor");
        if (divisor == 0) {
            throw C0deException.of(ErrorCode.DIVISION_BY_ZERO, "Division by 0");
        }
        if (dividend == Long.MIN_VALUE && divisor == -1) {
            throw overflow();
        }
        return new IntegerValue(dividend / divisor);
    }
}
