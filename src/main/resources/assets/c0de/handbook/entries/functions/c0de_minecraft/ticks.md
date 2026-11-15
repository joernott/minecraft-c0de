```json
{
  "title": "TICKS",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 1
}
```

Returns the game time of the world in ticks.

It counts all ticks since the world was created, 20 per second while the world runs, and it is the same in every dimension. Sleeping, `/time set` and the gamerule doDaylightCycle don't change it. So it is not the time of day.

There is no function for the time of day. `TICKS \ 24000` only gives it in a world where nobody ever slept or used `/time set` (0 is sunrise, 6000 noon, 18000 midnight). A daylight detector tells day from night: GET\_BLOCK gives its signal as text in `properties["power"]`, 0 at night.

- result `result` INTEGER: the game time in ticks

```c0de
CALL c0de/minecraft/TICKS
  RESULT result ticks
```
