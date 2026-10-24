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
| `c0de/minecraft/ENTITY` | uuid STRING, type STRING, name STRING, position STRUCT `c0de/minecraft/POSITION` | An entity. `position` is the block position the entity is in. |

## Owner and permissions

Every trigger block has an owner: the player who configured it last. All functions of the library act on behalf of this player.

Functions that change the world are restricted. A function of a restricted group can only be used if the owner is an administrator (operator with permission level 2 or higher) or if the group is enabled in the configuration file of the mod. The rights of an administrator only count for programs on sheets that belong to them, to another administrator or to nobody ([Sheet owners](01-dimension.md#sheet-owners)); otherwise another player could write a program with the name of an administrator's program and run it with the administrator's trigger block. Anyone may activate a trigger block, like a command block, but only its owner's rights count:

| Configuration key | Default | Functions |
|---|---|---|
| `allowInventoryChanges` | false | MOVE_ITEMS, DELETE_ITEMS |
| `allowBlockChanges` | false | PLACE_BLOCK, REMOVE_BLOCK |
| `allowSpawning` | false | SPAWN_ITEM, SPAWN_ENTITY |
| `allowTeleporting` | false | TELEPORT |

Calling a restricted function without the permission raises PERMISSION_DENIED. Independent of the configuration,
  - a player can only be teleported if the owner is an administrator,
  - blocks on the sheets of the c0de dimension can't be changed by any function (PERMISSION_DENIED),
  - functions never load chunks: accessing a position in a chunk that is not loaded raises CHUNK_NOT_LOADED,
  - changes SHOULD be made as the owner, so that spawn protection and protection mods apply to them.

Note: the current implementation makes the changes directly in the level, not as the owner. Spawn protection and protection mods are therefore not consulted; the permissions above are the only restriction.

## Functions

| Function | Parameters | Results | Description | AmigaBASIC |
|---|---|---|---|---|
| TICKS | – | result INTEGER | Game time of the dimension of the trigger block in ticks. | – |
| BEEP | – | – | Plays a short "pling" (the note block bell sound) at the trigger block. | BEEP |
| SOUND | note INTEGER, instrument STRING (default `harp`) | – | Plays a note at the trigger block like a note block: `note` MUST be between 0 and 24, `instrument` is the name of a note block instrument (e.g. `harp`, `bass`, `bell`, `flute`); the name is not case-sensitive. A note outside 0–24 or an unknown instrument raises INVALID_ARGUMENT. | SOUND |
| SAY | message STRING, player STRING (default empty) | – | Writes `message` to the chat, prefixed with `[<program>/<PID>]`. If `player` is set, only that player receives the message (INVALID_ARGUMENT if the player is not online), otherwise all players within the radius set by the configuration key `sayRadius` (default 64 blocks) around the trigger block. | SAY (speech output) |
| GET_BLOCK | position POSITION | id STRING, properties HASH STRING, power INTEGER, has_inventory BOOLEAN | The block at `position`: its ID (e.g. `minecraft:oak_door`), its block state properties as text (e.g. `facing` → `north`), the redstone signal strength it receives, i.e. the strongest signal from its neighbouring blocks (0–15), and whether it has an inventory that GET_INVENTORY, MOVE_ITEMS and DELETE_ITEMS can use. | – |
| GET_INVENTORY | position POSITION | result ARRAY STRUCT ITEM_STACK | The content of the inventory of the block at `position`, one element per slot. Raises INVALID_ARGUMENT if the block has no inventory. | – |
| GET_LEVER | position POSITION | result BOOLEAN | True if the lever at `position` is on. | – |
| SET_LEVER | position POSITION, on BOOLEAN | – | Switches the lever at `position` on or off. A lever on the sheets of the c0de dimension raises PERMISSION_DENIED. | – |
| GET_BUTTON | position POSITION | result BOOLEAN | True if the button at `position` is pressed. | – |
| PRESS_BUTTON | position POSITION | – | Presses the button at `position`, like a player would. A button on the sheets of the c0de dimension raises PERMISSION_DENIED. | – |
| GET_PRESSURE_PLATE | position POSITION | result INTEGER | The signal strength (0–15) of the pressure plate at `position`. Pressure plates can only be read; they are pressed by entities. | – |
| GET_OPEN | position POSITION | result BOOLEAN | True if the door, trapdoor or fence gate at `position` is open. | – |
| SET_OPEN | position POSITION, open BOOLEAN | – | Opens or closes the door, trapdoor or fence gate at `position`. Iron doors and iron trapdoors, which players can't open by hand, require the permission `allowBlockChanges`. A block on the sheets of the c0de dimension raises PERMISSION_DENIED. | – |
| MOVE_ITEMS | from POSITION, to POSITION, id STRING, count INTEGER | result INTEGER | Moves up to `count` items with the ID `id` from the inventory at `from` to the inventory at `to`. Returns the number of items that were moved. Restricted. | – |
| DELETE_ITEMS | position POSITION, id STRING, count INTEGER | result INTEGER | Removes up to `count` items with the ID `id` from the inventory at `position`. Returns the number of removed items. Restricted. | – |
| PLACE_BLOCK | position POSITION, id STRING, properties HASH STRING (default empty) | – | Places the block `id` with the given block state properties at `position`, which MUST contain air or a replaceable block such as grass or water, otherwise INVALID_ARGUMENT is raised. An unknown block, a property the block does not have or an invalid property value raises INVALID_ARGUMENT. Restricted. | – |
| REMOVE_BLOCK | position POSITION, drop BOOLEAN (default true) | – | Removes the block at `position`. If `drop` is true, the block drops as an item like when it is mined. Restricted. | – |
| SPAWN_ITEM | position POSITION, id STRING, count INTEGER | result STRING | Spawns an item entity with `count` items of `id` at `position` and returns its UUID. `count` MUST be between 1 and 64, otherwise INVALID_ARGUMENT is raised. Restricted. | – |
| SPAWN_ENTITY | position POSITION, type STRING | result STRING | Spawns an entity of the type `type` (e.g. `minecraft:pig`) at `position` and returns its UUID. An unknown type or a type that can't be spawned raises INVALID_ARGUMENT. Restricted. | – |
| FIND_ENTITIES | position POSITION, radius INTEGER, type STRING (default empty) | result ARRAY STRUCT ENTITY | All entities (including items) within `radius` blocks of `position`, optionally only those of the type `type` (a full ID such as `minecraft:pig`). `radius` MUST be between 1 and 64, otherwise INVALID_ARGUMENT is raised. The cursor and mark entities of c0de are not returned. | – |
| TELEPORT | uuid STRING, position POSITION | – | Teleports the entity or item with the UUID `uuid` to `position`. Raises INVALID_ARGUMENT if `uuid` is not a UUID or no such entity is loaded. Restricted; players can only be teleported if the owner is an administrator. | – |

In the table, POSITION, ITEM_STACK and ENTITY stand for the struct types `c0de/minecraft/POSITION`, `c0de/minecraft/ITEM_STACK` and `c0de/minecraft/ENTITY`.

For the functions that work with a specific kind of block (levers, buttons, pressure plates, doors, trapdoors, fence gates, inventories), a block of the wrong kind at `position` raises INVALID_ARGUMENT. An unknown item ID in `id` (MOVE_ITEMS, DELETE_ITEMS, SPAWN_ITEM) also raises INVALID_ARGUMENT.

The positions in the results (the `position` of an ENTITY) have an empty `dimension` if they are in the dimension of the trigger block.
