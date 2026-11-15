```json
{
  "title": "COMMAND",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 23
}
```

Runs a command like a command block.

The command runs at the trigger block, with the rights of a command block, and the name of the program and PID, e.g. `farm/3`. Only administrators may use COMMAND, as owner and as the player who started the process, and only if command blocks are enabled on the server. Otherwise PERMISSION\_DENIED is raised. The server can also switch it off in `config/c0de.properties`.

A command that fails is not an error: `result` is 0 and `output` tells why. The commands are written to the log of the server.

- `command` STRING: the command, with or without `/`
- result `result` INTEGER: how often it succeeded, like the comparator signal of a command block (1 for a query like `time query daytime` that succeeded, not the value it asked for)
- result `output` STRING: the last message of the command, in the language of the server (English on a dedicated server, the language of the game in single player)

```c0de
CALL c0de/minecraft/COMMAND
  PARAMETER command "/weather clear" ;
  RESULT result done
  RESULT output message
```
