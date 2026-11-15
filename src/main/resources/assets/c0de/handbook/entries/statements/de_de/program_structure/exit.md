```json
{
  "title": "EXIT",
  "icon": "c0de:exit_block",
  "category": "program_structure",
  "ordinal": 4
}
```

EXIT beendet sofort den ganzen Prozess.

Das funktioniert, egal wie tief der Cursor in Funktionen und Codeblöcken steckt. Danach folgt ein Ausdruck mit einer Zahl von 0 bis 15: der Exit-Code. Ein Komparator neben dem Auslöserblock kann ihn auslesen. EXIT funktioniert nur innerhalb von Funktionen.

Exit-Code 15 bedeutet, dass das Programm abgestürzt ist. Du kannst `EXIT 15 ;` auch für ein schwerwiegendes Problem verwenden.

EXIT ist kein Absturz: Die Ergebnisse der Einstiegsfunktion kommen mit den Werten heraus, die sie in diesem Moment haben.

<recipe;c0de:exit_block>

Lege eine Eisentür in die Mitte und acht NOOP-Blöcke darum herum. Du bekommst acht EXIT-Blöcke.

```c0de
EXIT 3 ;
ASSIGN value 0 ;
```

Die ASSIGN-Zeile läuft nie, weil EXIT den Prozess vorher beendet.
