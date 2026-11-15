```json
{
  "title": "Aufrufen",
  "icon": "c0de:include_block",
  "category": "libraries",
  "ordinal": 0
}
```

Alle diese Funktionen werden mit [CALL](^language/call) aufgerufen, wie deine eigenen Funktionen. [Eingebaute Funktionen](^libraries/built_in_functions) benutzen ihren einfachen Namen. Bibliotheksfunktionen benutzen ihren vollen Namen, zum Beispiel `c0de/strings/MID`.

```c0de
CALL c0de/strings/MID
  PARAMETER text name ;
  PARAMETER start 2 ;
  PARAMETER length 3 ;
  RESULT result part
```

Eine Funktion mit einem Ergebnis nennt es `result`. Positionen in Texten und Listen beginnen bei 0.
