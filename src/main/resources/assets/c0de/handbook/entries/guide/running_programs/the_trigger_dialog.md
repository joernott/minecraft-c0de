```json
{
  "title": "The trigger dialog",
  "icon": "c0de:trigger_block",
  "category": "running_programs",
  "ordinal": 1
}
```

Right-click a [trigger block](^running_programs/three_trigger_blocks) with an empty hand to open its dialog. It shows:

- the PID of the running process
- the [exit code](^running_programs/exit_codes) of the last process, and its error if it crashed
- a list to pick the program
- a list to pick the function to run
- for timer blocks, the interval in ticks

The dialog also has an input inventory, an output inventory and a slot for camouflage. Put any block into that slot, and the trigger block takes on its look.

The program and function lists are locked while a process runs.

**Recipes**

<item;c0de:trigger_block>

<recipe;c0de:trigger_block>

<item;c0de:timer_block>

<recipe;c0de:timer_block>
