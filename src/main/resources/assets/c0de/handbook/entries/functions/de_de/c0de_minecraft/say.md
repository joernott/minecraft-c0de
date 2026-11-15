```json
{
  "title": "SAY",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 4
}
```

Schreibt eine Nachricht in den Chat.

Der Chat zeigt `[program/PID] message`, z. B. `[farm/3] The farm is full!`, ohne den Namen eines Spielers davor. Mit `player` bekommt nur dieser Spieler sie, in jeder Dimension\; wenn er nicht online ist, wird INVALID\_ARGUMENT ausgelöst.

Ohne `player` bekommen sie alle Spieler in der Dimension des Auslöserblocks, die höchstens 64 Blöcke von ihm entfernt sind. Der Server kann die Entfernung mit der Einstellung sayRadius ändern.

- `message` STRING: die Nachricht
- `player` STRING: der Name eines Spielers (optional, Standard leer für alle Spieler in der Nähe)

```c0de
CALL c0de/minecraft/SAY
  PARAMETER message "The farm is full!" ;
  PARAMETER player "Alex" ;
```
