```json
{
  "title": "Trigger block",
  "icon": "c0de:trigger_block",
  "category": "starting_programs",
  "ordinal": 0
}
```

The trigger block starts a process when it gets a redstone pulse.

Only a signal that comes on starts it. Switching a lever off doesn't, and a signal that stays on doesn't start it again.

Right-click it with an empty hand to open its dialog. Pick a program and a function. Put variable and constant items into the input inventory. Results land in the output inventory. Items the process doesn't use pass through to the output unchanged.

The **Run** button decides who may start it: \*Only me\*, \*Players\*, \*Mobs\* or \*Everyone\*. The block knows who pressed a button or lever touching it, or who stands on a pressure plate next to it. Other redstone signals only start it with \*Everyone\*. Only you and administrators can change a trigger block you set up. If a player who is not an administrator starts it, functions that need more rights stop the program.

A hopper above or beside it fills the input, a hopper below it empties the output. An adjacent trigger block gets the output in its input, so you can build chains. A comparator reads the exit code of the last process as signal strength, and 0 while a process runs. It reads timer blocks and debug trigger blocks the same way.

<recipe;c0de:trigger_block>

Copper ingots in the corners, glass on the edges and a gold ingot in the center give eight trigger blocks.

Place a trigger block with a button on it. Set it to the function `answer` and put an uninitialized variable item into its input. Press the button. The output now holds an item named `result` with the value 42.

```c0de
PROGRAM first
NOOP
NOOP
FUNCTION answer
  RESULT result INTEGER
BEGIN
  ASSIGN result 42 ;
END FUNCTION
```
