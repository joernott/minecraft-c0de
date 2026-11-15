```json
{
  "title": "TYPE_ARRAY",
  "icon": "c0de:type_array_block",
  "category": "types",
  "ordinal": 6
}
```

TYPE\_ARRAY steht für den Typ ARRAY: eine Liste von Werten.

Ein Array ist ab 0 nummeriert. Es wächst und schrumpft von selbst. Hinter ARRAY steht der Typ seines Inhalts, zum Beispiel `ARRAY INTEGER`. Arrays aus Arrays sind erlaubt, bis zu 8 Ebenen tief.

<recipe;c0de:type_array_block>

Stelle eine Typbasis und ein Fass zusammen her.

```c0de
VARIABLE list ARRAY INTEGER
BEGIN
  1 2 3
END VARIABLE
ASSIGN list[3] 4 ;
```

Eine Zuweisung an den Index direkt hinter dem Ende fügt ein neues Element hinzu.
