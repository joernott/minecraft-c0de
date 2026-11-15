```json
{
  "title": "HOUR",
  "icon": "minecraft:book",
  "category": "c0de_time",
  "ordinal": 4
}
```

Returns the hour of a time (0 to 23).

Without `time` the function uses the current time. It uses the time zone of the server.

- `time` TIME: the time (optional, default now)
- result `result` INTEGER: the hour, 0 to 23

```c0de
CALL c0de/time/HOUR
  RESULT result hour
```
