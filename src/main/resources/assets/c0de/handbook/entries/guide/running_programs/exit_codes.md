```json
{
  "title": "Exit codes",
  "icon": "c0de:trigger_block",
  "category": "running_programs",
  "ordinal": 8
}
```

Every process ends with an **exit code** from 0 to 15. It is 0 when the program ends normally, the number from an [EXIT](^language/exit) statement, or 15 when the process crashed.

A comparator reading the [trigger block](^running_programs/three_trigger_blocks) puts out a signal as strong as the exit code. The signal stays until the next process starts, and it is 0 while a process runs. So your redstone can react to the result of a program. A crash gives a signal too (15): if any signal opens a door, a program that crashes opens it as well. Let a program that opens something end with a small exit code, e.g. `EXIT 1`, and compare the signal, e.g. with a second comparator. A comparator reads timer blocks and debug trigger blocks the same way.

**Recipes**

<item;c0de:exit_block>

<recipe;c0de:exit_block>

<item;c0de:trigger_block>

<recipe;c0de:trigger_block>

<item;c0de:timer_block>

<recipe;c0de:timer_block>

<item;c0de:debug_trigger_block>

<recipe;c0de:debug_trigger_block>
