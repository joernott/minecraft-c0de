```json
{
  "title": "SLEEP",
  "icon": "minecraft:book",
  "category": "c0de_time",
  "ordinal": 11
}
```

Pauses the program for a number of game ticks.

20 ticks are about one second. `ticks` must be between 1 and 72000 (one hour), otherwise INVALID\_ARGUMENT is raised.

- `ticks` INTEGER: the number of game ticks, 1 to 72000

```c0de
CALL c0de/time/SLEEP
  PARAMETER ticks 20 ;
```
