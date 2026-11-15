```json
{
  "title": "WEEK",
  "icon": "minecraft:book",
  "category": "c0de_time",
  "ordinal": 9
}
```

Returns the ISO 8601 week of the year (1 to 53).

Week 1 contains the first Thursday of the year. So days around New Year can belong to week 52 or 53 of the old year or week 1 of the new one. Without `time` the current time is used. It uses the time zone of the server.

There is no function for the year a week belongs to. On 1 January 2027, WEEK gives 53, the last week of 2026, but YEAR gives 2027. So if WEEK is 52 or 53 in January, the week belongs to the year before\; if it is 1 in December, to the year after.

- `time` TIME: the time (optional, default now)
- result `result` INTEGER: the week, 1 to 53

```c0de
CALL c0de/time/WEEK
  RESULT result week
```
