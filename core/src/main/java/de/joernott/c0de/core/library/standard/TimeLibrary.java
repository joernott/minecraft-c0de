package de.joernott.c0de.core.library.standard;

import static de.joernott.c0de.core.library.standard.Functions.INTEGER;
import static de.joernott.c0de.core.library.standard.Functions.TIME;
import static de.joernott.c0de.core.library.standard.Functions.integer;
import static de.joernott.c0de.core.library.standard.Functions.invalid;
import static de.joernott.c0de.core.library.standard.Functions.optional;
import static de.joernott.c0de.core.library.standard.Functions.required;

import de.joernott.c0de.core.library.Library;
import de.joernott.c0de.core.library.NativeCall;
import de.joernott.c0de.core.library.NativeFunction;
import de.joernott.c0de.core.library.NativeResult;
import de.joernott.c0de.core.value.FloatValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.TimeValue;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalField;
import java.util.List;
import java.util.Map;

/**
 * The library {@code c0de/time} (docs/concept/11-lib-time.md). All functions use the time zone of the
 * server.
 */
public final class TimeLibrary implements Library {
    public static final String NAME = "c0de/time";

    /** Longest SLEEP: one hour in game ticks. */
    public static final long MAX_SLEEP_TICKS = 72_000;

    private static final List<NativeFunction.Parameter> TIME_PARAMETER = List.of(optional("time", "TIME", TIME));

    private static final Map<String, NativeFunction> FUNCTIONS = new Functions.Builder()
            .add("NOW", List.of(), call -> new TimeValue(call.process().clock().millis()))
            .add("MILLISECOND", TIME_PARAMETER, part(ChronoField.MILLI_OF_SECOND))
            .add("SECOND", TIME_PARAMETER, part(ChronoField.SECOND_OF_MINUTE))
            .add("MINUTE", TIME_PARAMETER, part(ChronoField.MINUTE_OF_HOUR))
            .add("HOUR", TIME_PARAMETER, part(ChronoField.HOUR_OF_DAY))
            .add("DAY", TIME_PARAMETER, part(ChronoField.DAY_OF_MONTH))
            .add("MONTH", TIME_PARAMETER, part(ChronoField.MONTH_OF_YEAR))
            .add("YEAR", TIME_PARAMETER, part(ChronoField.YEAR))
            .add("WEEKDAY", TIME_PARAMETER, part(ChronoField.DAY_OF_WEEK))
            .add("WEEK", TIME_PARAMETER, part(IsoFields.WEEK_OF_WEEK_BASED_YEAR))
            .add("TIMER", List.of(), call -> {
                ZonedDateTime now = dateTime(call);
                return new FloatValue(now.toLocalTime().toNanoOfDay() / 1e9);
            })
            .add("SLEEP", List.of(required("ticks", "INTEGER", INTEGER)), List.of(), call -> {
                long ticks = integer(call, "ticks");
                if (ticks < 1 || ticks > MAX_SLEEP_TICKS) {
                    throw invalid("SLEEP waits 1 to " + MAX_SLEEP_TICKS + " ticks, not " + ticks);
                }
                return NativeResult.waitSteps((int) ticks);
            })
            .build();

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Map<String, NativeFunction> functions() {
        return FUNCTIONS;
    }

    private static Functions.ResultFunction part(TemporalField field) {
        return call -> new IntegerValue(dateTime(call).get(field));
    }

    /** The time of the parameter {@code time}, or now, in the time zone of the server. */
    private static ZonedDateTime dateTime(NativeCall call) {
        long millis = call.provided("time")
                ? ((TimeValue) call.argument("time")).millis()
                : call.process().clock().millis();
        return Instant.ofEpochMilli(millis).atZone(call.process().zone());
    }
}
