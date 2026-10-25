# c0de

**Build programs out of blocks.** c0de is a small programming language for Minecraft: you place language blocks on sheets of greenbar paper in their own dimension and run your programs with trigger blocks anywhere in your world.

c0de is inspired by BASIC and the early Lego Mindstorms. You don't need to know how to program. Every statement is a block, every block is crafted from vanilla materials, and the floor tells you right away whether a line is correct.

> This is the first alpha, version 0.1.0. Expect rough edges, and please report bugs and ideas on GitHub.

## Features

- **The c0de dimension**: endless sheets of green-and-white computer paper. One sheet, one program. No mobs, no weather, no night.
- **A real language**: variables and constants, integers, floats, text, time, arrays, hashes, structs, expressions with many operators, functions with parameters and results, recursion, IF/ELSE, FOR, EACH, WHILE, REPEAT and error handling.
- **Live checking**: line numbers on the floor turn green, yellow or red while you build, and the debugging tool tells you what is wrong.
- **Trigger blocks**: start programs with a redstone pulse, a timer, or step by step with a debug trigger. A comparator reads the exit code.
- **Data as items**: pass values in and out as variable and constant items. Hoppers and pipes can fill and empty trigger blocks, so programs can be chained.
- **Input and output**: PRINT text on display walls, laptops and tablets, let players type values for INPUT, and WRITE results into I/O blocks. Screen functions like CLS, LOCATE, COLOR and INKEY come from AmigaBASIC.
- **Libraries**: math, strings (including FORMAT, JSON and YAML), date and time, and a Minecraft library for sounds, chat messages, levers, buttons, doors, inventories, blocks and entities.
- **Permissions**: world-changing functions are off by default and can be enabled per server. Programs act on behalf of the trigger block's owner.
- **Editing tools**: a text marker to select lines, a wrench to move and copy them, a hammer to delete or insert lines.
- **c0de disk and disk drive**: copy programs onto disks, build them on empty sheets, and keep them as YAML files in the `c0de` folder of your own Minecraft to share them.
- **Debugging**: a red debug cursor you can pause and step line by line, and you can inspect every block and every cursor.

## Getting started

1. Craft a **trigger block** (copper ingots, glass, a gold ingot) and a **wrench** (iron ingots and red wool), and place the trigger block.
2. Right-click the trigger block with the wrench. You are teleported to a free sheet in the c0de dimension.
3. Craft NOOP blocks (iron ingots and sticks) and turn them into language blocks. Build a small program, for example a function that assigns 42 to its result.
4. Leave the dimension by right-clicking the black floor with an empty hand. Right-click the trigger block, pick your program and function, and put an uninitialized variable item (a data item and light blue dye) into its input.
5. Give the trigger block a redstone pulse. The result appears as a variable item in its output inventory.

## Documentation

The player guide explains everything step by step, with example programs:

- [Player guide](https://github.com/joernott/minecraft-c0de/tree/main/docs/guide)
- [Getting started](https://github.com/joernott/minecraft-c0de/blob/main/docs/guide/01-getting-started.md)
- [The language](https://github.com/joernott/minecraft-c0de/blob/main/docs/guide/02-language.md)
- [Running programs](https://github.com/joernott/minecraft-c0de/blob/main/docs/guide/03-running-programs.md)
- [Tools and items](https://github.com/joernott/minecraft-c0de/blob/main/docs/guide/04-tools.md)
- [Libraries](https://github.com/joernott/minecraft-c0de/blob/main/docs/guide/05-libraries.md)
- [Examples](https://github.com/joernott/minecraft-c0de/blob/main/docs/guide/06-examples.md)
- [Reference](https://github.com/joernott/minecraft-c0de/blob/main/docs/reference/index.md): a page for every block, item and function
- [Writing libraries](https://github.com/joernott/minecraft-c0de/blob/main/docs/library-development.md): for mod developers who want to add c0de libraries

## Requirements

- Minecraft 1.21.1
- **Fabric** with [Fabric API](https://modrinth.com/mod/fabric-api), or **NeoForge**
- Optional: [Patchouli](https://modrinth.com/mod/patchouli) adds an in-game guide book with the player guide. Every player gets it on the first join; more copies are crafted from a book and a PROGRAM block. Six handbooks (Data, Statements, Tools, Triggers, Devices and Functions) describe every block, item and function on a page of its own. On a NeoForge server, install Patchouli on the server too, otherwise clients with Patchouli can't join.
