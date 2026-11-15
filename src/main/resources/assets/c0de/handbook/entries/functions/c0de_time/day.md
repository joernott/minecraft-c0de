```json
{
  "title": "DAY",
  "icon": "minecraft:book",
  "category": "c0de_time",
  "ordinal": 5
}
```

Returns the day of the month (1 to 31).

Without `time` the function uses the current time. It uses the time zone of the server.

- `time` TIME: the time (optional, default now)
- result `result` INTEGER: the day, 1 to 31

```c0de
CALL c0de/time/DAY
  PARAMETER time birthday ;
  RESULT result day
```
