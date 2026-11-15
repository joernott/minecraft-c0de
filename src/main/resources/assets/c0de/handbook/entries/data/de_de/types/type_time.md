```json
{
  "title": "TYPE_TIME",
  "icon": "c0de:type_time_block",
  "category": "types",
  "ordinal": 9
}
```

TYPE\_TIME steht für den Typ TIME: einen Zeitpunkt.

Eine neue TIME enthält den Beginn des Jahres 1970 (UTC). Du kannst Millisekunden als INTEGER addieren oder abziehen. Zieht man zwei Zeiten voneinander ab, erhält man den Unterschied in Millisekunden.

Zeiten kannst du mit `=`, `<`, `>` und den anderen vergleichen: Die frühere Zeit ist die kleinere. `⊕` schreibt eine TIME als ISO-Text in UTC mit Millisekunden, zum Beispiel `2026-10-19T12:05:09.250Z`. FORMAT aus c0de/strings schreibt sie in der Zeitzone des Servers.

<recipe;c0de:type_time_block>

Stelle eine Typbasis und eine Uhr zusammen her.

```c0de
VARIABLE start TIME @2026-10-19T12:05:09Z
VARIABLE later TIME
ASSIGN later start + 60000 ;
```

Jetzt liegt `later` eine Minute nach `start`.
