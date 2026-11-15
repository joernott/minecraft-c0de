```json
{
  "title": "Bibliotheken einbinden",
  "icon": "c0de:include_block",
  "category": "libraries",
  "ordinal": 2
}
```

INCLUDE-Zeilen kommen direkt nach dem [NOOP](^language/noop_and_comment) in Zeile 1, vor jeder anderen Anweisung. Sie beginnen bei x=5, wie die PROGRAM-Zeile.

```c0de
PROGRAM math
NOOP
INCLUDE c0de/math
INCLUDE c0de/strings
```

Der LABEL-Dialog neben einem INCLUDE-Block zeigt alle Bibliotheken, aus denen du wählen kannst.
