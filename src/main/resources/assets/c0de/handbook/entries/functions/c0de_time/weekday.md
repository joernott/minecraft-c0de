```json
{
  "title": "WEEKDAY",
  "icon": "minecraft:book",
  "category": "c0de_time",
  "ordinal": 8
}
```

Returns the day of the week, from 1 (Monday) to 7 (Sunday).

Without `time` the function uses the current time. It uses the time zone of the server.

- `time` TIME: the time (optional, default now)
- result `result` INTEGER: the day of the week, 1 to 7

```c0de
CALL c0de/time/WEEKDAY
  PARAMETER time t ;
  RESULT result weekday
```
