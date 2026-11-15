```json
{
  "title": "PID-Blöcke",
  "icon": "c0de:trigger_block",
  "category": "running_programs",
  "ordinal": 13
}
```

Jeder Prozess bekommt eine Nummer, seine **PID**. Wenn er startet, erscheint ein dunkler PID-Block auf der Cursorschiene in Zeile 0. Seine Höhe ist die PID: der erste freie Platz, von y=0 aufwärts gezählt.

Der PID-Block zeigt die PID in Grün auf seiner Nord- und Südseite. Seine Ost- und Westseite zeigen, wo der [Auslöserblock](^running_programs/three_trigger_blocks) steht.

Mehrere Prozesse eines Programms können gleichzeitig laufen, einer pro Auslöserblock. Jeder hat seinen eigenen PID-Block und seinen eigenen Cursor. Sie teilen sich keine Variablen.

**Rezepte**

<item;c0de:trigger_block>

<recipe;c0de:trigger_block>
