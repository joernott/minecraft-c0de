```json
{
  "title": "Strukturen",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 6
}
```

Ein STRUCT deklariert deinen eigenen Typ. Jede Zeile in seinem [Codeblock](^language/lines_and_indentation) ist ein Element: ein Name und ein Typ.

```c0de
STRUCT point
BEGIN
  x INTEGER
  y INTEGER
END STRUCT
```

Eine Variable dieses Typs wird als `VARIABLE p STRUCT point` deklariert. Ihre Elemente erreichst du mit einem Index, zum Beispiel `p.x`.

Jedes Programm hat außerdem ein paar Konstanten, die es nicht deklariert. `SHEET` beschreibt sein Blatt, z. B. `SHEET.number` und `SHEET.owner`. `PID` beschreibt den laufenden Prozess, z. B. `PID.pid`, `PID.activator` (der Spieler, der ihn gestartet hat, leer bei anderem Redstone und Zeitgeberblöcken) und `PID.x`, `PID.y`, `PID.z` (wo sein [Auslöserblock](^running_programs/three_trigger_blocks) steht). `CURSOR` beschreibt den Cursor im Moment: `CURSOR.line`, `CURSOR.function`, `CURSOR.depth` und wie viel von seinem Stack belegt ist, `CURSOR.stack_used` von `CURSOR.stack_size`. `C0DE_VERSION` enthält die Version von c0de, z. B. "0.4.0". Auch die Namen der Fehler und der Farben sind Konstanten.

**Rezepte**

<item;c0de:type_struct_block>

<recipe;c0de:type_struct_block>

<item;c0de:timer_block>

<recipe;c0de:timer_block>

<item;c0de:trigger_block>

<recipe;c0de:trigger_block>
