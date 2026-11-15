```json
{
  "title": "Laptop",
  "icon": "c0de:laptop",
  "category": "screens",
  "ordinal": 1
}
```

Ein Laptop ist ein Block mit Bildschirm und Tastatur. Spieler tippen darauf die Werte für INPUT ein.

Klicke ihn mit rechts an, um sein Terminal zu öffnen. Klicke ihn mit dem Schraubenschlüssel mit links an, um ihn zu drehen. Der Bildschirm hat 48 Spalten und 20 Zeilen. Zeilen, die oben hinausrollen, bleiben in einem Verlauf von 200 Zeilen erhalten. Scrolle mit dem Mausrad oder der Bildlaufleiste.

Oben wählen zwei Knöpfe das Programm und die Funktion. ✔ bindet den Laptop und schließt das Terminal. Unter dem Bildschirm ist die Eingabezeile. Sie funktioniert nur, während ein INPUT wartet, und sie zeigt, was INPUT abfragt, zum Beispiel `INPUT count (INTEGER):`.

Drücke Enter, um die Zeile abzuschicken. Die Zeile erscheint auf dem Bildschirm. Text, der nicht zum Typ passt, wird mit einer Zeile abgelehnt, die mit `? ` beginnt, und INPUT wartet weiter. Tasten, die du tippst, während kein INPUT wartet, werden für INKEY aufbewahrt. Enter kommt dort als CHR 13 an.

Ein Laptop nimmt an WRITE nicht teil. Wenn du einen Laptop abbaust, wird sein Terminal gelöscht.

<recipe;c0de:laptop>

<recipe;c0de:laptop_from_tablet>

Stelle einen Anzeigeblock und einen E/A-Block zusammen her, oder verwandle ein Tablet in einen Laptop.

Binde einen Laptop an die Funktion `ask` und starte sie. Die Eingabezeile zeigt `INPUT count (INTEGER):`. Tippe 21 und drücke Enter. Der Bildschirm zeigt 42.

```c0de
PROGRAM typing
NOOP
NOOP
FUNCTION ask
BEGIN
  VARIABLE count INTEGER
  CALL INPUT
    PARAMETER variable count ;
  CALL PRINT
    PARAMETER value count * 2 ;
END FUNCTION
```
