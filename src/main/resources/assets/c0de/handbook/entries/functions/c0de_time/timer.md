```json
{
  "title": "TIMER",
  "icon": "minecraft:book",
  "category": "c0de_time",
  "ordinal": 10
}
```

Returns the seconds since midnight.

This is the real time of the server, not the game time. Use TICKS from c0de/minecraft for the game time.

TIMER starts at 0.0 at midnight in the time zone of the server and counts up to below 86400, with the milliseconds as decimals. On the days the clocks change for summer time, it follows the clock.

- result `result` FLOAT: the seconds since midnight

```c0de
CALL c0de/time/TIMER
  RESULT result seconds
```
