```json
{
  "title": "Mit Spielern sprechen",
  "icon": "c0de:trigger_block",
  "category": "running_programs",
  "ordinal": 11
}
```

Ein Prozess kann auch mit Spielern sprechen, während er läuft. Drei eingebaute Funktionen tun das, aufgerufen mit CALL wie jede andere Funktion:

- **PRINT** schreibt Text auf Bildschirme.
- **INPUT** liest einen Wert in eine Variable.
- **WRITE** legt einen Wert als Variablenkarte in einen E/A-Block.

Die Geräte dafür sind Anzeigeblöcke, Laptops, Tablets und E/A-Blöcke. Verbinde jedes davon mit einem Programm und einer Funktion, wie einen Auslöserblock. Anzeigeblöcke nebeneinander bilden einen großen Bildschirm.

COLOR ändert die Farben des Texts, der danach kommt. Die Konstanten COLOR\_BLACK bis COLOR\_WHITE benennen die 16 Farben:

```c0de
PROGRAM hello
NOOP
FUNCTION greet
BEGIN
  CALL CLS
  CALL COLOR
    PARAMETER foreground COLOR_YELLOW ;
    PARAMETER background COLOR_DARK_BLUE ;
  CALL PRINT
    PARAMETER value "Process " ;
  CALL PRINT
    PARAMETER value PID.pid ;
  CALL COLOR
    PARAMETER foreground COLOR_WHITE ;
END FUNCTION
```

INPUT nimmt zuerst eine passende Karte aus einem E/A-Block. Sonst wartet es, bis ein Spieler auf einem Laptop oder Tablet eine Zeile eintippt. Die getippte Zeile erscheint auf seinem Bildschirm, du siehst also deine Antwort. Die Handbücher Funktionen und Geräte beschreiben sie alle.
