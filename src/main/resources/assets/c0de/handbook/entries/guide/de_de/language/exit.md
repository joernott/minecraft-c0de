```json
{
  "title": "EXIT",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 19
}
```

EXIT beendet sofort den ganzen Prozess, egal wie tief der Cursor steckt. Es braucht eine Zahl von 0 bis 15: den **Exit-Code**. Ein Komparator neben dem Auslöserblock kann ihn lesen.

```c0de
EXIT 3 ;
```

Exit-Code 15 bedeutet, dass das Programm abgestürzt ist. Du darfst `EXIT 15 ;` auch für ein schweres Problem verwenden.

EXIT ist kein Absturz: Die Ergebnisse der Einstiegsfunktion kommen trotzdem heraus, mit den Werten, die sie in diesem Moment haben. Nur ein echter Absturz liefert keine Ergebnisse.

**Rezepte**

<item;c0de:exit_block>

<recipe;c0de:exit_block>

<item;c0de:trigger_block>

<recipe;c0de:trigger_block>
