```json
{
  "title": "E/A-Block",
  "icon": "c0de:io_block",
  "category": "items",
  "ordinal": 0
}
```

Der E/A-Block nimmt Werte für INPUT an und gibt Werte von WRITE aus.

Klicke ihn mit rechts an, um seinen Dialog zu öffnen. Er hat 18 Eingabeplätze und 18 Ausgabeplätze. Die Eingabe nimmt nur Variablen-, Konstanten- und Literalkarten an. Aus der Ausgabe kannst du Gegenstände nur herausnehmen. Die Knöpfe Programm und Funktion und ✔ binden den Block, wie bei einem Anzeigeblock.

Trichter füllen die Eingabe und leeren die Ausgabe. Ein Komparator liest, wie voll die Ausgabe ist.

<recipe;c0de:io_block>

Stelle eine Datenkarte und einen Trichter zusammen her.

Binde einen E/A-Block an die Funktion `double`. Lege eine INTEGER-Variablenkarte namens `number` mit dem Wert 21 und eine nicht initialisierte Variablenkarte in seine Eingabe. Starte die Funktion: Die Ausgabe bekommt eine Karte namens `output` mit dem Wert 42.

```c0de
PROGRAM doubling
NOOP
NOOP
FUNCTION double
BEGIN
  VARIABLE number INTEGER
  CALL INPUT
    PARAMETER variable number ;
  CALL WRITE
    PARAMETER value number * 2 ;
END FUNCTION
```
