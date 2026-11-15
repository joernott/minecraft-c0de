```json
{
  "title": "Debugging tool",
  "icon": "c0de:debug_tool",
  "category": "editing_tools",
  "ordinal": 5
}
```

The debugging tool lets you look inside blocks and cursors and run a program step by step.

Right-click any c0de block or cursor to see its properties. Hit a debug trigger block to start a process with a red debug cursor. It starts paused. Left-click it to move one line. Shift-left-click it to let it run or pause it again.

Each left click on a cursor also shows its current line in your chat. A wrong line is shown in red, an unfinished statement in gold. Shift-right-click a PID block to abort its process. Only the owner of the trigger block and operators can do this.

<recipe;c0de:debug_tool>

A red wool block bottom left, a stick in the middle and a leather top right.

A line number is red and you don't know why. Right-click a block of the line with the debugging tool. The dialog tells you what is wrong.

Your program gives a wrong result. Start it with a debug trigger block and step through it line by line. Right-click the cursor to see the values on its stack and how many of its slots are used. If a deep recursion comes close to the size of the stack, the process crashes with STACK\_OVERFLOW.
