```json
{
  "title": "Anzeigeblock",
  "icon": "c0de:display_block",
  "category": "screens",
  "ordinal": 0
}
```

Der Anzeigeblock zeigt den Text von PRINT auf seiner Vorderseite.

Ein Anzeigeblock hat 16 Spalten und 8 Zeilen. Anzeigeblöcke, die nebeneinander ein flaches Rechteck bilden und alle in dieselbe Richtung zeigen, ergeben eine Anzeigewand mit einem großen Bildschirm. Eine Wand aus 4 × 2 Blöcken hat 64 Spalten und 16 Zeilen. Eine Wand kann aufrecht stehen oder flach liegen.

Klicke einen Anzeigeblock mit leerer Hand mit rechts an, um seinen Dialog zu öffnen. Klicke auf die Knöpfe Programm und Funktion, bis sie dein Programm und deine Funktion zeigen. Klicke dann auf ✔, um den Block zu binden. Bindest du einen Block, ist die ganze Wand gebunden. Ein neuer Block, der zu einer Wand dazukommt, übernimmt ihre Bindung.

Der Dialog hat auch einen Platz für die Tarnung. Lege einen vollen Block hinein, und die Seiten des Anzeigeblocks nehmen sein Aussehen an. Nimm ihn wieder heraus, um die Tarnung zu entfernen. Jeder Block einer Wand hat seine eigene Tarnung. Klicke einen Anzeigeblock mit dem Schraubenschlüssel mit links an, um ihn zu drehen.

<recipe;c0de:display_block>

Sieben Eisenklumpen um einen Glowstonestaub in der Mitte, mit einem Redstone-Staub links davon, ergeben acht Anzeigeblöcke.

Baue eine Wand aus 2 × 1 Anzeigeblöcken und binde sie an die Funktion `welcome`. Starte die Funktion mit einem Auslöserblock. Die Wand zeigt "Welcome\!" in Gelb.

```c0de
PROGRAM sign
NOOP
NOOP
FUNCTION welcome
BEGIN
  CALL CLS
  CALL COLOR
    PARAMETER foreground 14 ;
  CALL PRINT
    PARAMETER value "Welcome!" ;
END FUNCTION
```
