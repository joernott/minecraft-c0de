```json
{
  "title": "CHR",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 10
}
```

Gibt das Zeichen zu einem Unicode-Codepunkt zurück.

Eine Zahl, die kein Codepunkt ist, löst INVALID\_ARGUMENT aus.

- `code` INTEGER: der Codepunkt
- Ergebnis `result` CHAR: das Zeichen

```c0de
CALL c0de/strings/CHR
  PARAMETER code 65 ;
  RESULT result letter
```
