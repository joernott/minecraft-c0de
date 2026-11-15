```json
{
  "title": "STR",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 11
}
```

Wandelt einen Wert in Text um.

Der Text ist derselbe, den der Operator ⊕ erzeugt.

- `value` BOOLEAN|INTEGER|FLOAT|CHAR|TIME: der Wert
- Ergebnis `result` STRING: der Wert als Text

```c0de
CALL c0de/strings/STR
  PARAMETER value score ;
  RESULT result scoreText
```
