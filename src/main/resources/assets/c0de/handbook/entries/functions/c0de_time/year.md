```json
{
  "title": "YEAR",
  "icon": "minecraft:book",
  "category": "c0de_time",
  "ordinal": 7
}
```

Returns the year of a time, e.g. 2026.

Without `time` the function uses the current time. It uses the time zone of the server.

- `time` TIME: the time (optional, default now)
- result `result` INTEGER: the year

```c0de
CALL c0de/time/YEAR
  RESULT result year
```
