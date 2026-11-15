```json
{
  "title": "COMMAND",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 23
}
```

Führt einen Befehl aus wie ein Befehlsblock.

Der Befehl läuft am Auslöserblock, mit den Rechten eines Befehlsblocks und dem Namen des Programms und der PID, z. B. `farm/3`. Nur Administratoren dürfen COMMAND verwenden, als Besitzer und als der Spieler, der den Prozess gestartet hat, und nur wenn Befehlsblöcke auf dem Server aktiviert sind. Sonst wird PERMISSION\_DENIED ausgelöst. Der Server kann die Funktion auch in `config/c0de.properties` abschalten.

Ein Befehl, der fehlschlägt, ist kein Fehler: `result` ist 0, und `output` sagt, warum. Die Befehle werden in das Log des Servers geschrieben.

- `command` STRING: der Befehl, mit oder ohne `/`
- Ergebnis `result` INTEGER: wie oft er erfolgreich war, wie das Komparatorsignal eines Befehlsblocks
- Ergebnis `output` STRING: die letzte Meldung des Befehls

```c0de
CALL c0de/minecraft/COMMAND
  PARAMETER command "/weather clear" ;
  RESULT result done
  RESULT output message
```
