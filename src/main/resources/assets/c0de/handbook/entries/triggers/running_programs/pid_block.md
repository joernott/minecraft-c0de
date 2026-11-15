```json
{
  "title": "PID block",
  "icon": "minecraft:book",
  "category": "running_programs",
  "ordinal": 0
}
```

The PID block marks a running process on the cursor rail of its sheet.

When a process starts, a dark PID block appears at line 0 of the cursor rail. Its height is the PID, the number of the process. Its north and south faces show the PID in green. Its east and west faces show where the trigger block stands.

While a PID block is there, the sheet is locked: nobody can place or break blocks on it. Shift-right-click the PID block with the debugging tool to abort the process. Only the owner of the trigger block and operators can do this.

Not craftable.

A program runs in an endless loop and the trigger block can't start it again. Find its PID block, shift-right-click it with the debugging tool, and the process stops. Its items go to the output inventory.

Several processes of one program can run at once, one per trigger block. Each has its own PID block and cursor.
