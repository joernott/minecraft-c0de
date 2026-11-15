```json
{
  "title": "FOR-Schleifen",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 15
}
```

FOR zählt. Es hat eine Schleifenvariable, ihren Typ (INTEGER oder FLOAT), einen Startwert und einen Endwert. Ein optionaler dritter Wert ist die Schrittweite, die 1 ist, wenn du sie weglässt.

Der Endwert ist eingeschlossen. Du kannst die Schleifenvariable nicht selbst ändern.

```c0de
FOR i INTEGER 1 ; 10 ; 2 ;
BEGIN
  NOOP
END FOR
```
