```json
{
  "title": "MONTH",
  "icon": "minecraft:book",
  "category": "c0de_time",
  "ordinal": 6
}
```

Returns the month of a time (1 to 12).

Without `time` the function uses the current time. It uses the time zone of the server.

- `time` TIME: the time (optional, default now)
- result `result` INTEGER: the month, 1 to 12

```c0de
CALL c0de/time/MONTH
  PARAMETER time birthday ;
  RESULT result month
```
