# Running programs

[c0de concept](../concept.md) › Running programs

## Trigger blocks

Processes are started by trigger blocks, which can be placed in any dimension. In the c0de dimension, they can only be placed in the area with negative x or z coordinates (the black area outside of the sheets). There are three kinds of trigger blocks:
  - A **trigger block** starts a process when it receives a redstone pulse.
  - A **timer block** starts a process at a configurable interval.
  - A **debug trigger block** starts a process with a [debug cursor](15-cursor.md#movement) when a player attacks it with a debugging tool.

A trigger block can't start a process if
  - it is not configured yet,
  - the program or function it has been configured with has been deleted since,
  - a process it started is still running,
  - no process ID is available for the program, i.e. all y coordinates between 0 and the maximum build height above the PID position of the program are occupied,
  - the input inventory does not contain an item for every parameter of the function that has no default value,
  - the function has more results than parameters and the input inventory does not contain enough empty variable items to make up the difference,
  - there are items in its output inventory, or
  - (timer block only) it receives a redstone signal. A redstone signal pauses a timer block.

Each trigger block has an owner (the player who configured it last, see [Owner and permissions](12-lib-minecraft.md#owner-and-permissions)), a property that contains the PID while a process is running, two properties that specify the program and the function to run, and a property that contains the exit code and the error of the last process. A timer block also has a property specifying the interval between activations. The user can set these properties in a dialog that opens when the block is right-clicked. The dialog has the following elements:
  - a display of the PID of the currently running process
  - a display of the exit code and, if the last process crashed, its error code, message, context and line
  - a dropdown to select the name of the program to run. It is locked while a process is running and is populated with the names of the programs in the c0de dimension.
  - a dropdown to select the function to run. It is locked while a process is running or if no program has been selected. It is populated with the functions of the selected program. Changing the program empties and repopulates this dropdown.
  - (timer block only) an input field for the interval in ticks
  - a chest-sized input inventory. Only variable and constant items (not to be confused with VARIABLE and CONSTANT blocks) can be inserted here.
  - a chest-sized output inventory
  - a single-slot inventory that accepts a single block item. Placing a block there changes the texture of the trigger block to that of the inserted block, camouflaging it.

## Starting a process

Starting a process places a PID block on the cursor rail next to the PROGRAM statement (x=4, z=0 relative to the sheet). Its y coordinate is determined by searching for the first air block from y=0 up to the maximum build height. This y coordinate is the PID. A cursor is spawned in the line after it (x=4, y=PID, z=1) and initialized with the data it needs from the trigger block: the dimension and coordinates of the trigger block, the name of the program and the name of the function to run.

The chunks of a sheet with a running process MUST stay loaded until the process has ended.

## Protection of running programs

While at least one process of a program is running, its sheet MUST NOT be changed. In particular,
  - no block can be placed on or removed from the sheet (x=0 to x=31, from the floor up to the maximum build height), neither by players nor by any other means such as pistons, explosions, fluids, commands of other mods or block-placing entities,
  - the dialogs of LABEL, LITERAL and OPERATOR blocks on the sheet open read-only,
  - the wrench and the hammer can't be used on the sheet.

The text marker can still be used, as it does not change the program. Players who try to change a protected sheet get a message that names the running processes (program name and PIDs).

## Exchanging data with a process

Data is passed into a process as variable and constant items in the input inventory of the trigger block. Each item is assigned to the parameter of the entry function whose name matches the name property of the item. Its type MUST match the type of the parameter.

When a process ends normally, one initialized variable item per result of the entry function is moved from the stack of the cursor to the output inventory. If the function has fewer results than parameters, empty variable items are added to make up the difference, so the number of items going into and out of a process is the same. All other items of the input inventory are moved to the output inventory.

If there is exactly one block with an inventory adjacent to the trigger block and that inventory has sufficient capacity, all items in the output inventory are transferred to it automatically.

## Ending a process

A process ends when
  - the END of the entry function is reached,
  - a RETURN statement is executed in the entry function,
  - an EXIT statement is executed,
  - the cursor crashes, or
  - the cursor reaches an empty line (which means the entry function was not found; the cursor crashes with UNKNOWN_FUNCTION).

When a process ends, the exit code (0 if no EXIT statement was executed, 15 if the cursor crashed) and, if the cursor crashed, the error are stored in the trigger block. A comparator reading the trigger block outputs a signal strength equal to the exit code of the last process until the next process is started. If the cursor crashed, all items of the input inventory are moved to the output inventory unchanged and no result items are created. The cursor entity and the PID block are removed from the c0de dimension.

## Minecraft blocks

### Trigger block

A non-camouflaged trigger block looks like a transparent block with copper corners and a golden ball in its center. When camouflaged, it looks like the block it is camouflaged as.

#### Crafting

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "redstone",
  "pattern": [
    "ABA",
    "BCB",
    "ABA"
  ],
  "key": {
    "A": "minecraft:copper_ingot",
    "B": "minecraft:glass",
    "C": "minecraft:gold_ingot"
  },
  "result": {
    "id": "c0de:trigger_block",
    "count": 8
  }
}
```

### Timer block

A non-camouflaged timer block looks like a transparent block with copper corners and a Minecraft clock in its center. When camouflaged, it looks like the block it is camouflaged as.

#### Crafting

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "redstone",
  "pattern": [
    "ABA",
    "BCB",
    "ABA"
  ],
  "key": {
    "A": "minecraft:copper_ingot",
    "B": "minecraft:glass",
    "C": "minecraft:clock"
  },
  "result": {
    "id": "c0de:timer_block",
    "count": 8
  }
}
```

### Debug trigger block

A non-camouflaged debug trigger block looks like a transparent block with copper corners and a red ball in its center. When camouflaged, it looks like the block it is camouflaged as.

#### Crafting

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "redstone",
  "pattern": [
    "ABA",
    "BCB",
    "ABA"
  ],
  "key": {
    "A": "minecraft:copper_ingot",
    "B": "minecraft:glass",
    "C": "minecraft:redstone_block"
  },
  "result": {
    "id": "c0de:debug_trigger_block",
    "count": 1
  }
}
```

### PID block

A PID block is an almost black block (color #222222). Its northern and southern faces show its own y coordinate (the PID) in green (color #55FF55), its eastern and western faces show the dimension and coordinates of the trigger block that started the process. This block can't be crafted and can't be destroyed by any entity.

## Configuration file

The settings of the mod are stored in the file `config/c0de.properties` of the server (or of the client for single player worlds). The file is created with the default values and a description of every setting when it doesn't exist. Invalid values are reported in the log and replaced by their default values.

| Setting | Default | Description |
|---|---|---|
| `maxBuildHeight` | 255 | Highest usable y coordinate of the c0de dimension (15 to 2031, a multiple of 16 minus 1). It limits the number of processes per program. Changes apply after a restart; lowering the value in an existing world removes all blocks above the new height from the c0de dimension. |
| `stackSize` | 2048 | Number of slots of the stack of a cursor (2048 to 1048576). |
| `sayRadius` | 64 | Radius in blocks for messages of SAY without a player name (1 to 1024). |
| `allowInventoryChanges` | false | See [Owner and permissions](12-lib-minecraft.md#owner-and-permissions). |
| `allowBlockChanges` | false | See [Owner and permissions](12-lib-minecraft.md#owner-and-permissions). |
| `allowSpawning` | false | See [Owner and permissions](12-lib-minecraft.md#owner-and-permissions). |
| `allowTeleporting` | false | See [Owner and permissions](12-lib-minecraft.md#owner-and-permissions). |
