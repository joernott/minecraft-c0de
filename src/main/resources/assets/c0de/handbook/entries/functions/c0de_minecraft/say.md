```json
{
  "title": "SAY",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 4
}
```

Writes a message to the chat.

The chat shows `[program/PID] message`, e.g. `[farm/3] The farm is full!`, without the name of a player in front. With `player`, only that player gets it, in any dimension\; if they are not online, INVALID\_ARGUMENT is raised.

Without `player`, all players in the dimension of the trigger block get it who are at most 64 blocks away from it. The server can change the distance with the setting sayRadius.

- `message` STRING: the message
- `player` STRING: the name of one player (optional, default empty for all players nearby)

```c0de
CALL c0de/minecraft/SAY
  PARAMETER message "The farm is full!" ;
  PARAMETER player "Alex" ;
```
