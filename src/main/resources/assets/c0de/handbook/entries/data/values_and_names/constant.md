```json
{
  "title": "CONSTANT",
  "icon": "c0de:constant_block",
  "category": "values_and_names",
  "ordinal": 3
}
```

A CONSTANT line makes a constant: a named value that never changes.

It looks like a VARIABLE line, but it must have a value. The value is a LITERAL, not a calculation and not another constant\; lists, hashes and structs get theirs in a code block, like a VARIABLE. You can't ASSIGN to a constant, and it can't receive a CALL result.

Some constants exist in every program. The names of the errors, like `DIVISION_BY_ZERO`, hold their codes. `COLOR_BLACK` to `COLOR_WHITE` are the colors for COLOR. `SHEET` tells a program about its sheet: `number`, `program`, `owner` and the position `x`, `y`, `z` of its first floor block. `PID` tells it about its process: `pid`, `program`, `function`, `owner`, `activator` (the player who started it, empty for other redstone signals, mobs and timer blocks), the position `x`, `y`, `z` and `dimension` of the trigger block, `debug` and the tick it `started`. `CURSOR` tells it about its cursor right now: the `line`, the `function`, the `depth` of the calls, and how many slots of the stack are used (`stack_used`) of `stack_size`. Their types are SHEET\_INFO, PID\_INFO and CURSOR\_INFO. `C0DE_VERSION` is the version of c0de as text, like "0.4.0". Other mods can add constants of their own, like `C0DE_NET_VERSION` of c0de net.

<recipe;c0de:constant_block>

<recipe;c0de:constant_block_from_variable_block>

Craft a type base and three bones, or turn a VARIABLE block into a CONSTANT block.

```c0de
CONSTANT BASE INTEGER 42
CONSTANT ORIGIN STRUCT point
BEGIN
  x 3
END CONSTANT
```
