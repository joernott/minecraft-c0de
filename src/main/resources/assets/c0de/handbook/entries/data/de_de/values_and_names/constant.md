```json
{
  "title": "CONSTANT",
  "icon": "c0de:constant_block",
  "category": "values_and_names",
  "ordinal": 3
}
```

Eine CONSTANT-Zeile erzeugt eine Konstante: einen benannten Wert, der sich nie ändert.

Sie sieht aus wie eine VARIABLE-Zeile, muss aber einen Wert haben. Der Wert ist ein LITERAL, keine Rechnung und keine andere Konstante\; Listen, Hashes und Strukturen bekommen ihren in einem Codeblock, wie eine VARIABLE. Du kannst einer Konstante nichts mit ASSIGN zuweisen, und sie kann kein Ergebnis eines CALL aufnehmen.

Einige Konstanten gibt es in jedem Programm. Die Namen der Fehler, zum Beispiel `DIVISION_BY_ZERO`, enthalten ihre Codes. `COLOR_BLACK` bis `COLOR_WHITE` sind die Farben für COLOR. `SHEET` erzählt einem Programm von seinem Blatt: `number`, `program`, `owner` und die Position `x`, `y`, `z` seines ersten Bodenblocks. `PID` erzählt ihm von seinem Prozess: `pid`, `program`, `function`, `owner`, `activator` (der Spieler, der ihn gestartet hat, leer bei anderen Redstone-Signalen, Mobs und Zeitgeberblöcken), die Position `x`, `y`, `z` und `dimension` des Auslöserblocks, `debug` und der Tick, in dem er gestartet ist (`started`). `CURSOR` erzählt ihm, wo sein Cursor gerade steht: die Zeile (`line`), die Funktion (`function`), die Tiefe der Aufrufe (`depth`) und wie viele Plätze des Stacks belegt sind (`stack_used`) von `stack_size`. Ihre Typen sind SHEET\_INFO, PID\_INFO und CURSOR\_INFO. `C0DE_VERSION` ist die Version von c0de als Text, zum Beispiel "0.4.0". Andere Mods können eigene Konstanten hinzufügen, zum Beispiel `C0DE_NET_VERSION` von c0de net.

<recipe;c0de:constant_block>

<recipe;c0de:constant_block_from_variable_block>

Stelle eine Typbasis und drei Knochen zusammen her, oder verwandle einen VARIABLE-Block in einen CONSTANT-Block.

```c0de
CONSTANT BASE INTEGER 42
CONSTANT ORIGIN STRUCT point
BEGIN
  x 3
END CONSTANT
```
