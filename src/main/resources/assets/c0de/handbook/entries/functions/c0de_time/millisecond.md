```json
{
  "title": "MILLISECOND",
  "icon": "minecraft:book",
  "category": "c0de_time",
  "ordinal": 1
}
```

Returns the milliseconds of a time (0 to 999).

Without `time` the function uses the current time. It uses the time zone of the server.

- `time` TIME: the time (optional, default now)
- result `result` INTEGER: the millisecond, 0 to 999

```c0de
CALL c0de/time/MILLISECOND
  PARAMETER time start ;
  RESULT result millis
```
