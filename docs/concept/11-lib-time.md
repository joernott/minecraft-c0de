# The c0de/time library

[c0de concept](../concept.md) › The c0de/time library

The library `c0de/time` contains the time and date functions. It MUST be included with `INCLUDE c0de/time`. The game time of Minecraft is provided by the [c0de/minecraft library](12-lib-minecraft.md) (TICKS).

Points in time are stored in variables of the type [TIME](04-data.md#time), which contain a Unix timestamp in milliseconds. Dates and times are formatted as text with the function [FORMAT](10-lib-strings.md#format) of the c0de/strings library, e.g. `%tF` for the date and `%tT` for the time.

The functions that return a part of a date or time have an optional parameter `time`. Without it, they use the current time. All of them, and FORMAT, always use the time zone of the server.

| Function | Parameters | Results | Description | AmigaBASIC |
|---|---|---|---|---|
| NOW | – | result TIME | The current time as Unix timestamp in milliseconds. | – |
| MILLISECOND | time TIME (default: now) | result INTEGER | The milliseconds passed in the current second (0–999). | – |
| SECOND | time TIME (default: now) | result INTEGER | The seconds passed in the current minute (0–59). | – |
| MINUTE | time TIME (default: now) | result INTEGER | The minute in the hour (0–59). | – |
| HOUR | time TIME (default: now) | result INTEGER | The hour in the day in 24-hour format (0–23). | – |
| DAY | time TIME (default: now) | result INTEGER | The day in the month (1–31). | – |
| MONTH | time TIME (default: now) | result INTEGER | The month (1–12). | – |
| YEAR | time TIME (default: now) | result INTEGER | The year, e.g. 2026. | – |
| WEEKDAY | time TIME (default: now) | result INTEGER | The day of the week according to ISO 8601, from 1 (Monday) to 7 (Sunday). | – |
| WEEK | time TIME (default: now) | result INTEGER | The week of the year according to ISO 8601 (1–53). The first week of a year is the week that contains the first Thursday of the year, so the days around New Year's Day can belong to week 52 or 53 of the previous year or to week 1 of the next year. | – |
| TIMER | – | result FLOAT | Seconds since midnight (real time of the server). | TIMER |
| SLEEP | ticks INTEGER | – | Pauses the cursor for `ticks` game ticks. `ticks` MUST be between 1 and 72000 (one hour). | SLEEP |

The AmigaBASIC functions DATE$ and TIME$ are replaced by FORMAT:

```
INCLUDE c0de/strings
INCLUDE c0de/time
…
CALL c0de/time/NOW
    RESULT result now
CALL c0de/strings/FORMAT
    PARAMETER value now ;
    PARAMETER format "%tF %tT" ;
    RESULT result text
```

After these calls, `text` contains e.g. `2026-10-19 14:05:09`.
