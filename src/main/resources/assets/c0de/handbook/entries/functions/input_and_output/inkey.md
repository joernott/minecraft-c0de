```json
{
  "title": "INKEY",
  "icon": "minecraft:book",
  "category": "input_and_output",
  "ordinal": 8
}
```

Returns the oldest key a player typed on a laptop or tablet.

Only keys typed while no INPUT waits count. Enter arrives as CHR 13. If no key is waiting, the result is the empty string. INKEY doesn't wait.

- result `result` STRING: the key, or the empty string

```c0de
CALL INKEY
  RESULT result key
```
