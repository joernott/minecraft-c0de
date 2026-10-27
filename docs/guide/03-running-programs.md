# Running Programs

A program on a sheet does nothing by itself. Trigger blocks start it, feed it data and collect its results. A running program is called a **process**.

## Three trigger blocks

- **trigger block**: starts a process when it gets a redstone pulse.
- **timer block**: starts a process again and again, at an interval you set.
- **debug trigger block**: starts a process when you hit it with the debugging tool.

Trigger blocks work in every dimension. In the c0de dimension, you can only place them on the black floor outside the sheets.

A redstone signal pauses a timer block. Use a lever to switch it off. As a sheet is protected while its program runs, pause a fast timer before you change the program; its dialog then shows "Paused by redstone".

## The trigger dialog

Right-click a trigger block with an empty hand to open its dialog. It shows:

- the PID of the running process
- the exit code of the last process, and its error if it crashed
- a list to pick the program
- a list to pick the function to run
- for timer blocks, the interval in ticks

The dialog also has an input inventory, an output inventory and a slot for camouflage. Put any block into that slot, and the trigger block takes on its look.

The program and function lists are locked while a process runs.

## The owner

The player who set up a trigger block last is its **owner**. Its programs act on behalf of this player. This matters for the c0de/minecraft library and for aborting processes.

## Variable items

Data goes into a process as **variable items** and **constant items** in the input inventory. Only these items fit there.

Each item has a name, a type and a value. An item goes to the parameter of the entry function with the same name. Its type must match the type of the parameter.

To edit a variable item, hold it in your main hand and right-click. The dialog lets you set its name, its type and its value. A constant item can only be edited until it has a value.

## Uninitialized items

An **uninitialized** variable item has no name, no type and no value. A process writes a result into it.

You need one for each result that has no parameter item to take its place. A function with one result and no parameters needs one uninitialized item.

Spare uninitialized items stay in the input inventory. So you can stack several of them there for many runs.

## What a process takes

When a process starts, it takes from the input inventory:

- one item for each parameter that has an item,
- one uninitialized item for each extra result,
- all other initialized variable and constant items.

Those other items are not used. They are passed through unchanged to the output inventory when the process ends.

## Results

When a process ends normally, the output inventory gets one variable item per result of the entry function. Each item carries the name, type and value of its result.

If the function has fewer results than parameters, you also get empty variable items back. So the number of items going in and out is the same.

## When it won't start

A trigger block does not start a process if:

- it has no program or function yet,
- its last process is still running,
- an item is missing for a parameter without default value,
- an uninitialized item is missing for a result,
- the output inventory has too few free slots.

## Exit codes

Every process ends with an **exit code** from 0 to 15. It is 0 when the program ends normally, the number from an EXIT statement, or 15 when the process crashed.

A comparator reading the trigger block puts out a signal as strong as the exit code. The signal stays until the next process starts. So your redstone can react to the result of a program.

## Crashes

When a process crashes, the trigger block plays a low note. Its dialog shows the error code, the message, the function and the line where it happened.

A crashed process creates no results. All items it took go to the output inventory unchanged.

## Chaining

Hoppers, pipes and cables can use a trigger block from all sides. A hopper above or beside it fills the input. A hopper below it empties the output.

So you can build chains: trigger block, hopper, trigger block, hopper, chest. Each process uses only the items it needs and passes on the rest. The end of the chain collects everything.

If exactly one block with an inventory touches a trigger block, the output goes there by itself. Another trigger block counts too: the items land in its input. A hopper pointing into the trigger block does not count.

## Talking to players

A process can also talk to players while it runs. Three built-in functions do this, called with CALL like any other function:

- **PRINT** writes text on screens.
- **INPUT** reads a value into a variable.
- **WRITE** puts a value into an I/O block as a variable item.

The devices for this are display blocks, laptops, tablets and I/O blocks. Bind each of them to a program and function, like a trigger block. Display blocks next to each other form one big screen.

COLOR changes the colors of the text that follows. The constants COLOR_BLACK to COLOR_WHITE name the 16 colors:

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

INPUT takes a matching item from an I/O block first. Otherwise it waits until a player types a line on a laptop or tablet. The handbooks Functions and Devices describe all of them.

## Protected programs

While a process runs, its sheet is locked. Nobody can place or break blocks on it, not even pistons or explosions. The wrench and the hammer don't work there.

Dialogs of blocks on the sheet open read-only. The text marker still works, because it changes nothing. If you try to change a locked sheet, you get a message naming the running processes.

## PID blocks

Each process gets a number, its **PID**. When it starts, a dark PID block appears on the cursor rail at line 0. Its height is the PID: the first free spot counted up from y=0.

The PID block shows the PID in green on its north and south faces. Its east and west faces show where the trigger block stands.

Several processes of one program can run at the same time, one per trigger block. Each has its own PID block and its own cursor. They don't share any variables.

## The cursor

The cursor is a glowing green pillar on the cursor rail. It sits in the line that is being worked on. It moves on by one line per tick.

A debug cursor glows red. You move it yourself with the debugging tool, see the chapter on tools.

## Aborting a process

Shift-right-click a PID block with the debugging tool to stop its process. The process crashes with the error PROCESS_ABORTED, and its items go to the output inventory.

Only the owner of the trigger block and server operators can do this.
