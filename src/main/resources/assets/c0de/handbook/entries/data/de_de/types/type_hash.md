```json
{
  "title": "TYPE_HASH",
  "icon": "c0de:type_hash_block",
  "category": "types",
  "ordinal": 7
}
```

TYPE\_HASH steht für den Typ HASH: Werte, die unter Textschlüsseln gespeichert sind.

Ein Hash wächst und schrumpft von selbst. Hinter HASH steht der Typ seines Inhalts, zum Beispiel `HASH INTEGER`. Ein neuer Schlüssel fügt ein neues Element hinzu.

<recipe;c0de:type_hash_block>

Stelle eine Typbasis und eine Truhe zusammen her. Eine Redstone-Truhe geht auch.

```c0de
VARIABLE stock HASH INTEGER
BEGIN
  gold 10
END VARIABLE
ASSIGN stock["diamond"] 3 ;
```
