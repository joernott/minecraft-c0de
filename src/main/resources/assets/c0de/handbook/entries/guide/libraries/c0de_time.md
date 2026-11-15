```json
{
  "title": "c0de/time",
  "icon": "c0de:include_block",
  "category": "libraries",
  "ordinal": 16
}
```

Real-world time and date, in the time zone of the server:

- NOW: the current time as a TIME value
- YEAR, MONTH, DAY: parts of the date
- HOUR, MINUTE, SECOND, MILLISECOND: parts of the time
- WEEKDAY: 1 for Monday up to 7 for Sunday
- WEEK: the week of the year
- TIMER: seconds since midnight
- SLEEP: wait for a number of game ticks

The date and time functions use the current time, or a TIME value you pass as `time`. SLEEP waits between 1 and 72000 ticks.

TIMER starts at 0 at midnight. WEEK gives the ISO week: on 1 January it can still be week 52 or 53 of the year before, and no function gives that year.
