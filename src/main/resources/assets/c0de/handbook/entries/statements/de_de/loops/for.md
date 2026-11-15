```json
{
  "title": "FOR",
  "icon": "c0de:for_block",
  "category": "loops",
  "ordinal": 0
}
```

FOR zählt mit einer Schleifenvariable.

Danach folgen ein Name, ein Typ (INTEGER oder FLOAT), ein Startwert und ein Endwert. Ein optionaler dritter Wert ist die Schrittweite. Ohne ihn ist die Schrittweite 1. Die Schrittweite darf nicht 0 sein, und eine negative Schrittweite zählt rückwärts.

Der Endwert gehört dazu. Die Werte werden einmal berechnet, bevor die Schleife beginnt. Du kannst die Schleifenvariable nicht selbst ändern.

<recipe;c0de:for_block>

<recipe;c0de:for_block_from_each_block>

Stelle eine Strukturbasis und einen Erdblock zusammen her, oder verwandle einen EACH-Block in einen FOR-Block.

```c0de
FOR i INTEGER 1 ; 10 ; 2 ;
BEGIN
  ASSIGN sum sum + i ;
END FOR
```

Die Schleife läuft mit i = 1, 3, 5, 7 und 9.
