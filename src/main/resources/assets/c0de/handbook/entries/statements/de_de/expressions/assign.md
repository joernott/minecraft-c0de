```json
{
  "title": "ASSIGN",
  "icon": "c0de:assign_block",
  "category": "expressions",
  "ordinal": 0
}
```

ASSIGN speichert den Wert eines Ausdrucks in einer Variable.

Danach folgt ein LABEL mit der Variable, dann der Ausdruck. Er endet mit `;`. Der Wert muss zum Typ passen: Ein INTEGER darf in ein FLOAT, und ein CHAR in einen STRING. Listen und andere zusammengesetzte Werte werden kopiert.

<recipe;c0de:assign_block>

Lege türkise Wolle in die Mitte und acht NOOP-Blöcke darum herum. Du bekommst acht ASSIGN-Blöcke.

```c0de
ASSIGN list[0] 10 ;
ASSIGN list[1] 20 ;
ASSIGN list[2] list[0] + list[1] ;
```
