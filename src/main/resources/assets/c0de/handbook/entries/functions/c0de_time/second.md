```json
{
  "title": "SECOND",
  "icon": "minecraft:book",
  "category": "c0de_time",
  "ordinal": 2
}
```

Returns the second of a time (0 to 59).

Without `time` the function uses the current time. It uses the time zone of the server.

- `time` TIME: the time (optional, default now)
- result `result` INTEGER: the second, 0 to 59

```c0de
CALL c0de/time/SECOND
  PARAMETER time start ;
  RESULT result second
```
