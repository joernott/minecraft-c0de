```json
{
  "title": "MINUTE",
  "icon": "minecraft:book",
  "category": "c0de_time",
  "ordinal": 3
}
```

Returns the minute of a time (0 to 59).

Without `time` the function uses the current time. It uses the time zone of the server.

- `time` TIME: the time (optional, default now)
- result `result` INTEGER: the minute, 0 to 59

```c0de
CALL c0de/time/MINUTE
  RESULT result minute
```
