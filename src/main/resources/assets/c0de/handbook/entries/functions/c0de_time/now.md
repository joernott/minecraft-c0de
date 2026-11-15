```json
{
  "title": "NOW",
  "icon": "minecraft:book",
  "category": "c0de_time",
  "ordinal": 0
}
```

Returns the current time.

The TIME value is a Unix timestamp in milliseconds. Use FORMAT to turn it into text.

- result `result` TIME: the current time

```c0de
CALL c0de/time/NOW
  RESULT result now
```
