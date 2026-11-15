```json
{
  "title": "Timer block",
  "icon": "c0de:timer_block",
  "category": "starting_programs",
  "ordinal": 1
}
```

The timer block starts a process again and again, at an interval you set.

Its dialog works like that of a trigger block, with one extra field: the interval in ticks. 20 ticks are one second. A new process only starts when the last one has ended.

A redstone signal pauses the timer block. Use a lever to switch it off and on. A sheet can't be changed while a process of its program runs, so pause a fast timer before you change its program\; the dialog then shows "Paused by redstone". A timer block placed next to a signal that is already on starts paused. Like the trigger block, it can be camouflaged with any block in the camouflage slot.

<recipe;c0de:timer_block>

Copper ingots in the corners, glass on the edges and a clock in the center give eight timer blocks.

Set a timer block to the function `ring` and an interval of 20 ticks. It plays a bell note every second. Put a lever next to it to stop the chime.

```c0de
PROGRAM chime
NOOP
INCLUDE c0de/minecraft
NOOP
FUNCTION ring
BEGIN
  CALL c0de/minecraft/SOUND
    PARAMETER note 12 ;
    PARAMETER instrument "bell" ;
END FUNCTION
```
