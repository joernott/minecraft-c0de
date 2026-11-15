```json
{
  "title": "Werte ändern",
  "icon": "c0de:include_block",
  "category": "libraries",
  "ordinal": 1
}
```

Funktionen bekommen Kopien ihrer Parameter. Eine Funktion, die einen Text, eine Liste oder einen Hash ändert, gibt die geänderte Kopie als `result` zurück.

Um eine Variable zu ändern, benutze sie sowohl als Parameter als auch als Ziel des Ergebnisses:

```c0de
CALL INSERT
  PARAMETER array list ;
  PARAMETER index 0 ;
  PARAMETER value 0 ;
  RESULT result list
```
