```json
{
  "title": "Talking to players",
  "icon": "c0de:trigger_block",
  "category": "running_programs",
  "ordinal": 11
}
```

A process can also talk to players while it runs. Three [built-in functions](^libraries/built_in_functions) do this, called with [CALL](^language/call) like any other function:

- **PRINT** writes text on screens.
- **INPUT** reads a value into a variable.
- **WRITE** puts a value into an I/O block as a [variable item](^tools/variable_items).

The devices for this are display blocks, laptops, tablets and I/O blocks. Bind each of them to a program and function, like a [trigger block](^running_programs/three_trigger_blocks). Display blocks next to each other form one big screen.

COLOR changes the colors of the text that follows. The constants COLOR\_BLACK to COLOR\_WHITE name the 16 colors:

```c0de
PROGRAM hello
NOOP
FUNCTION greet
BEGIN
  CALL CLS
  CALL COLOR
    PARAMETER foreground COLOR_YELLOW ;
    PARAMETER background COLOR_DARK_BLUE ;
  CALL PRINT
    PARAMETER value "Process " ;
  CALL PRINT
    PARAMETER value PID.pid ;
  CALL COLOR
    PARAMETER foreground COLOR_WHITE ;
END FUNCTION
```

INPUT takes a matching item from an I/O block first. Otherwise it waits until a player types a line on a laptop or tablet. The typed line appears on its screen, so you see your answer. The [handbooks](^tools/handbooks) Functions and Devices describe all of them.

**Recipes**

<item;c0de:call_block>

<recipe;c0de:call_block>

<item;c0de:io_block>

<recipe;c0de:io_block>

<item;c0de:variable_item>

<recipe;c0de:variable_item_uninitialized>

<item;c0de:display_block>

<recipe;c0de:display_block>

<item;c0de:laptop>

<recipe;c0de:laptop>

<item;c0de:tablet>

<recipe;c0de:tablet>

<item;c0de:trigger_block>

<recipe;c0de:trigger_block>
