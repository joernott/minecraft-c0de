```json
{
  "title": "ASC",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 9
}
```

Gibt den Unicode-Codepunkt eines Zeichens zurück.

- `char` CHAR: das Zeichen
- Ergebnis `result` INTEGER: sein Codepunkt, z. B. 65 für A

```c0de
CALL c0de/strings/ASC
  PARAMETER char letter ;
  RESULT result code
```
