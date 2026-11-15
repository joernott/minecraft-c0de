# The c0de/minecraft library

[c0de concept](../concept.md) › The c0de/minecraft library

All functions that interact with Minecraft are part of the optional library `c0de/minecraft`, which is provided by the c0de mod. A program that uses them MUST include the library with `INCLUDE c0de/minecraft`. Its functions are called by their qualified names:

```
CALL c0de/minecraft/SOUND
    PARAMETER note 12 ;
    PARAMETER instrument "bell" ;
```

The library follows the [conventions](08-builtin-functions.md) of all c0de functions, with one exception: unlike the standard libraries, its STRING parameters only accept a STRING, not a CHAR.

## Types

The library provides the following struct types:

| Type | Elements | Description |
|---|---|---|
| `c0de/minecraft/POSITION` | x INTEGER, y INTEGER, z INTEGER, dimension STRING | A block position. An empty `dimension` means the dimension of the trigger block. Otherwise it contains a dimension ID such as `minecraft:the_nether`; an ID that is not a dimension of the server raises INVALID_ARGUMENT. |
| `c0de/minecraft/ITEM_STACK` | id STRING, count INTEGER | The content of an inventory slot. An empty slot has the id `""` and the count 0. |
| `c0de/minecraft/ENTITY` | uuid STRING, type STRING, name STRING, position STRUCT `c0de/minecraft/POSITION` | An entity. `type` is its full type ID, e.g. `minecraft:sheep`. `name` is the name of a player, the custom name (name tag) or the name of the type, which can depend on the language. `position` is the block position the entity is in: the coordinates of the entity rounded down (x = -0.5 is block -1). |

## Mining

MINE_BLOCK chooses the tool a player would choose. It sorts the items of the tool inventory into four groups and takes the first item (lowest slot) of the best group:

1. Tools that obtain the block and break it faster than bare hands: a pickaxe for cobblestone, a shovel for dirt, shears for leaves.
2. Items that obtain the block when bare hands don't, e.g. a pickaxe with Silk Touch for glass. If bare hands obtain the block, they are used instead of these items.
3. Tools that break the block faster without obtaining it, if bare hands don't obtain it either: a stone pickaxe for obsidian.
4. Bare hands.

A tool obtains a block if mining it with the tool drops something: for blocks that need the correct tool (stone, ores, obsidian) that tool, for other blocks the block's own item (leaves with shears, dirt with bare hands). So for cobblestone, an iron pickaxe in slot 0 is taken before a stone pickaxe in slot 1 and a diamond pickaxe in slot 2; for obsidian, the diamond pickaxe, the only one that obtains it.

The time is that of a player standing on the ground without effects: 30 ticks (100 ticks without the correct tool) times the hardness of the block, divided by the speed of the tool including Efficiency, rounded up; blocks that break at once take no time. Enchantments such as Fortune, Silk Touch and Unbreaking work as for players; the experience of ores drops at the block.

## Owner and permissions

Every trigger block has an owner: the player who configured it last. All functions of the library act on behalf of this player.

Functions that change the world are restricted. A function of a restricted group can only be used if the owner is an administrator (operator with permission level 2 or higher) or if the group is enabled in the configuration file of the mod. The rights of an administrator only count for programs on sheets that belong to them, to another administrator or to nobody ([Sheet owners](01-dimension.md#sheet-owners)); otherwise another player could write a program with the name of an administrator's program and run it with the administrator's trigger block. If a player who is not an administrator started the process (the activator of the trigger block, see [Run](13-running-programs.md#trigger-block)), a restricted function that the configuration doesn't allow is a fatal error: the process crashes with PERMISSION_DENIED instead of receiving the error. Otherwise only the owner's rights count:

| Configuration key | Default | Functions |
|---|---|---|
| `allowInventoryChanges` | false | DELETE_ITEMS |
| `allowBlockChanges` | false | PLACE_BLOCK (without `inventory`, or with `data`), REMOVE_BLOCK |
| `allowSpawning` | false | SPAWN_ITEM, SPAWN_ENTITY |
| `allowTeleporting` | false | TELEPORT |

Calling a restricted function without the permission raises PERMISSION_DENIED. Independent of the configuration,
  - a player can only be teleported if the owner is an administrator,
  - COMMAND and the `data` of blocks that only operators may set (command blocks, structure blocks, jigsaw blocks) need an administrator, like the commands `/setblock` and `/data`,
  - blocks on the sheets of the c0de dimension can't be changed by any function (PERMISSION_DENIED),
  - functions never load chunks: accessing a position in a chunk that is not loaded raises CHUNK_NOT_LOADED,
  - changes SHOULD be made as the owner, so that spawn protection and protection mods apply to them.

Note: the current implementation makes the changes directly in the level, not as the owner. Spawn protection and protection mods are therefore not consulted; the permissions above are the only restriction. Only MOVE_ITEMS checks, like a player who opens the inventories, that the owner may reach them ([Moving items](#moving-items)).

Two functions can be switched off for everyone, administrators included:

| Configuration key | Default | Functions |
|---|---|---|
| `enableMoveItems` | true | MOVE_ITEMS |
| `enableCommand` | true | COMMAND |

A function that is switched off raises PERMISSION_DENIED.

## Block data

Blocks with a block entity, such as chests, signs, banners, furnaces or spawners, keep data that their block state doesn't show: the content of the inventory, the text, the patterns, the custom name. Minecraft stores it as NBT. GET_BLOCK returns it in its result `data`, PLACE_BLOCK takes it in its parameter `data`, both as SNBT, the text form of NBT that the commands `/data` and `/setblock` use:

```
{Items:[{Slot:0b,count:12,id:"minecraft:cobblestone"}]}
```

The data doesn't contain the position and the ID of the block entity (`x`, `y`, `z`, `id`), so it can be placed anywhere. Blocks without a block entity have the data `""`. The format of the data depends on the Minecraft version; data read on one version may not be accepted by another.

PLACE_BLOCK with `data` works like `/setblock <position> <id>[<properties>]<data>`: it places the block and then loads the data into its block entity. It raises INVALID_ARGUMENT before it changes anything if `data` is not SNBT of a compound, or if the block has no block entity, and INVALID_ARGUMENT if the block entity refuses the data (the block stays placed then). Data needs `allowBlockChanges` or an administrator, also with `inventory`, because it could create items; the data of command blocks, structure blocks and jigsaw blocks always needs an administrator.

## Moving items

MOVE_ITEMS moves items between two inventories in the same dimension. It isn't restricted, because a player can do the same by hand, but the owner of the trigger block must be able to open both inventories:

  - the blocks are not on the sheets of the c0de dimension,
  - the containers are not locked with a key (the `Lock` or `lock` of their data),
  - the blocks are not in the spawn protection, unless the owner is an operator, and within the world border.

Otherwise PERMISSION_DENIED is raised before anything is moved. The distance to the owner doesn't matter.

The items are taken from the slots of `from` in their order, starting with slot 0. They go first onto stacks of the same item in `to`, then into empty slots that accept them (e.g. only fuel in the fuel slot of a furnace). `count` is the number of items to move, or -1 for all items with the ID or that match the filter. MOVE_ITEMS moves as many as fit and returns the number it moved, which is smaller than `count` if `from` didn't have enough or `to` was full.

### Duration

Items travel like a player who carries them: the items are moved at once, then the cursor waits. One stack per tick travels up to 128 blocks:

`ticks = ceil(stacks / moveItemsStacksPerTick) × ceil(distance / moveItemsBlocksPerTick)`

`stacks` is the number of slots of `from` items were taken from, `distance` the distance between the centers of the two blocks. With the defaults, 3 stacks to a chest next to it take 3 ticks, over 300 blocks 9 ticks. If nothing was moved, the cursor doesn't wait. The two settings are in the configuration file.

### Filter

`id` selects the items of one item ID, e.g. `minecraft:wheat` (as before 0.6.0; an ID that is not an item raises INVALID_ARGUMENT). `filter` selects them more freely: a STRING or an ARRAY STRING. Only one of the two may be given. Without both, or with an empty filter, all items match. An array matches an item if one of its elements matches. Each string is either an item ID or a search expression like in the search field of JEI:

  - An item ID with a namespace, e.g. `minecraft:cobblestone`, matches exactly that item. An ID that is not an item raises INVALID_ARGUMENT.
  - Otherwise the string is a search expression. Words separated by spaces must all match; `|` separates alternatives, of which one must match. Text in quotes (`"red wool"`) is one word. Case doesn't matter.

| Word | Matches items whose |
|---|---|
| `text` | name contains `text` (the custom name of a renamed item, otherwise the English name) |
| `@text` | mod (the namespace of the ID) contains `text` |
| `$text` | tags: one of them contains `text`, e.g. `$logs` for `minecraft:logs` |
| `&text` | ID contains `text` |
| `-word` | the word doesn't match, e.g. `-@minecraft` |

The words of JEI for tooltips (`#`), creative tabs (`%`) and colors (`^`) need the client and raise INVALID_ARGUMENT, as does an empty word.

```
CALL c0de/minecraft/MOVE_ITEMS
    PARAMETER from input ;
    PARAMETER to ores ;
    PARAMETER count -1 ;
    PARAMETER filter "$ores | &raw_" ;
    RESULT result moved
```

## Commands

COMMAND runs a command like a command block at the trigger block: with the permission level 2 of command blocks, in the dimension of the trigger block, at the center of the trigger block, without an entity (`@s` selects nothing), and with the name `<program>/<PID>`, e.g. `/say` writes `[farm/3] ...`. It needs

  - an administrator as owner (with the rule for sheets above) and, if a player started the process, as activator (otherwise the process crashes),
  - command blocks enabled on the server: up to Minecraft 1.21.1 with `enable-command-block` in `server.properties` (always in single player worlds), since 1.21.11 with the game rule "Enable Command Blocks",
  - `enableCommand` in the configuration file.

Otherwise PERMISSION_DENIED is raised. A command that fails, e.g. because of a syntax error, is not an error of the program: its `result` is 0 and `output` contains the message. The commands are written to the log of the server with the program and PID. Like the messages of command blocks, their messages go to the operators in the chat if the game rule `commandBlockOutput` ("Broadcast command block output") is on.

## Functions

| Function | Parameters | Results | Description | AmigaBASIC |
|---|---|---|---|---|
| TICKS | – | result INTEGER | The game time of the world in ticks (the same in all dimensions): the number of ticks the world has run since it was created. It is not the time of day: sleeping, `/time set` and the gamerule `doDaylightCycle` don't change it. The library has no function for the time of day; `TICKS \ 24000` equals it only in a world in which nobody slept and `/time set` was never used, and GET_BLOCK of a daylight detector (property `power`) tells day from night. | – |
| BEEP | – | – | Plays a short "pling" (the note block bell sound) at the trigger block. | BEEP |
| SOUND | note INTEGER, instrument STRING (default `harp`) | – | Plays a note at the trigger block like a note block: `note` MUST be between 0 and 24, `instrument` is the name of a note block instrument: `harp`, `basedrum`, `snare`, `hat`, `bass`, `flute`, `bell`, `guitar`, `chime`, `xylophone`, `iron_xylophone`, `cow_bell`, `didgeridoo`, `bit`, `banjo`, `pling`, the mob head sounds `zombie`, `skeleton`, `creeper`, `dragon`, `wither_skeleton`, `piglin` and `custom_head` (a click), and since Minecraft 26.1 `trumpet`, `trumpet_exposed`, `trumpet_weathered` and `trumpet_oxidized`; the name is not case-sensitive. The pitch of `note` also applies to the mob head sounds. A note outside 0–24 or an unknown instrument raises INVALID_ARGUMENT. | SOUND |
| SAY | message STRING, player STRING (default empty) | – | Writes `message` to the chat as a system message (without a player name) in the format `[<program>/<PID>] <message>`, e.g. `[farm/3] The farm is full!`. If `player` is set, only that player receives the message, in whatever dimension they are (INVALID_ARGUMENT if the player is not online), otherwise all players in the dimension of the trigger block within the radius set by the configuration key `sayRadius` (default 64 blocks) around the center of the trigger block. | SAY (speech output) |
| GET_BLOCK | position POSITION | id STRING, properties HASH STRING, power INTEGER, has_inventory BOOLEAN, data STRING | The block at `position`: its ID (e.g. `minecraft:oak_door`), its block state properties as text (e.g. `facing` → `north`), the redstone signal strength it receives, i.e. the strongest signal from its neighbouring blocks (0–15), whether it has an inventory that GET_INVENTORY, MOVE_ITEMS and DELETE_ITEMS can use, and the [data](#block-data) of its block entity as SNBT (`""` without block entity). | – |
| GET_INVENTORY | position POSITION | result ARRAY STRUCT ITEM_STACK | The content of the inventory of the block at `position`, one element per slot. Raises INVALID_ARGUMENT if the block has no inventory. | – |
| GET_LEVER | position POSITION | result BOOLEAN | True if the lever at `position` is on. | – |
| SET_LEVER | position POSITION, on BOOLEAN | – | Switches the lever at `position` on or off. A lever on the sheets of the c0de dimension raises PERMISSION_DENIED. | – |
| GET_BUTTON | position POSITION | result BOOLEAN | True if the button at `position` is pressed. | – |
| PRESS_BUTTON | position POSITION | – | Presses the button at `position`, like a player would. A button on the sheets of the c0de dimension raises PERMISSION_DENIED. | – |
| GET_PRESSURE_PLATE | position POSITION | result INTEGER | The signal strength (0–15) of the pressure plate at `position`. Pressure plates can only be read; they are pressed by entities. | – |
| GET_OPEN | position POSITION | result BOOLEAN | True if the door, trapdoor or fence gate at `position` is open. | – |
| SET_OPEN | position POSITION, open BOOLEAN | – | Opens or closes the door, trapdoor or fence gate at `position`. Iron doors and iron trapdoors, which players can't open by hand, require the permission `allowBlockChanges`. A block on the sheets of the c0de dimension raises PERMISSION_DENIED. | – |
| MOVE_ITEMS | from POSITION, to POSITION, count INTEGER (default 1), id STRING (optional), filter STRING or ARRAY STRING (optional) | result INTEGER | Moves up to `count` items (-1: all) with the ID `id` or that match `filter` from the inventory at `from` to the inventory at `to` in the same dimension and returns the number of items it moved. Without `id` and `filter`, all items are moved; `id` and `filter` together raise INVALID_ARGUMENT. The cursor waits while the items travel. `count` 0 or below -1, the same block for `from` and `to`, or two dimensions raise INVALID_ARGUMENT, inventories the owner couldn't open PERMISSION_DENIED. See [Moving items](#moving-items). | – |
| DELETE_ITEMS | position POSITION, id STRING, count INTEGER | result INTEGER | Removes up to `count` items with the ID `id` from the inventory at `position`. Returns the number of removed items. Restricted. | – |
| PLACE_BLOCK | position POSITION, id STRING, properties HASH STRING (default empty), inventory POSITION (optional), data STRING (default empty) | – | Places the block `id` with the given block state properties at `position`, which MUST contain air or a replaceable block such as grass or water, otherwise INVALID_ARGUMENT is raised. An unknown block, a property the block does not have or an invalid property value raises INVALID_ARGUMENT. With `inventory`, the block is taken from the first stack of its item in that inventory, like a player places a block from their inventory: ITEM_MISSING is raised if there is none, and INVALID_ARGUMENT for blocks without item (e.g. fire). With `data`, the [data](#block-data) is loaded into the block entity of the new block. Restricted, unless `inventory` is given without `data`. | – |
| REMOVE_BLOCK | position POSITION, drop BOOLEAN (default true) | – | Removes the block at `position`. If `drop` is true, the block drops as an item like when it is mined. Restricted. | – |
| MINE_BLOCK | position POSITION, tool_inventory POSITION, output_inventory POSITION (optional) | – | Breaks the block at `position` like a player with the most suitable tool of `tool_inventory` (see below). The tool takes the damage it takes when a player uses it, and the cursor waits as many ticks as a player needs to break the block with it. The drops go into `output_inventory`, or onto the ground without it; if they don't fit, INVENTORY_FULL is raised before anything changes. Air is skipped: no tool is used and the cursor doesn't wait, but the positions and inventories are checked before (CHUNK_NOT_LOADED, PERMISSION_DENIED, INVALID_ARGUMENT). Unbreakable blocks (bedrock), fluids and the blocks that hold one of the two inventories raise INVALID_ARGUMENT. Not restricted, because it only does what a player can do. | – |
| SPAWN_ITEM | position POSITION, id STRING, count INTEGER | result STRING | Spawns an item entity with `count` items of `id` at `position` and returns its UUID. `count` MUST be between 1 and 64, otherwise INVALID_ARGUMENT is raised. Restricted. | – |
| SPAWN_ENTITY | position POSITION, type STRING | result STRING | Spawns an entity of the type `type` (e.g. `minecraft:pig`) at `position` and returns its UUID. An unknown type or a type that can't be spawned raises INVALID_ARGUMENT. Restricted. | – |
| FIND_ENTITIES | position POSITION, radius INTEGER, type STRING (default empty) | result ARRAY STRUCT ENTITY | All entities (including items) in the dimension of `position` whose position (the feet) is within a sphere of `radius` blocks around the center of the block at `position`, optionally only those of the type `type` (a full ID such as `minecraft:pig`). `radius` MUST be between 1 and 64, otherwise INVALID_ARGUMENT is raised. The cursor and mark entities of c0de are not returned. | – |
| COMMAND | command STRING | result INTEGER, output STRING | Runs `command` (with or without `/`) like a command block at the trigger block and returns its success count, which a comparator reads from a command block, and the text of its last message. Only for administrators; see [Commands](#commands). An empty command raises INVALID_ARGUMENT. | – |
| TELEPORT | uuid STRING, position POSITION | – | Teleports the entity or item with the UUID `uuid` to `position`, to the center of the bottom of the block. The entity may be in any dimension of the server, and `position` may be in another dimension, to which the entity moves. Raises INVALID_ARGUMENT if `uuid` is not a UUID or no such entity is loaded, and CHUNK_NOT_LOADED (without teleporting) if the chunk of `position` is not loaded. Restricted; players can only be teleported if the owner is an administrator. | – |

In the table, POSITION, ITEM_STACK and ENTITY stand for the struct types `c0de/minecraft/POSITION`, `c0de/minecraft/ITEM_STACK` and `c0de/minecraft/ENTITY`.

For the functions that work with a specific kind of block (levers, buttons, pressure plates, doors, trapdoors, fence gates, inventories), a block of the wrong kind at `position` raises INVALID_ARGUMENT. An unknown item ID in `id` (DELETE_ITEMS, SPAWN_ITEM) or in the filter of MOVE_ITEMS also raises INVALID_ARGUMENT.

The positions in the results (the `position` of an ENTITY) have an empty `dimension` if they are in the dimension of the trigger block.
