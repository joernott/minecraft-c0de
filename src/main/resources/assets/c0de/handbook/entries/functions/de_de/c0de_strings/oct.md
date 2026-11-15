```json
{
  "title": "OCT",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 15
}
```

Gibt den Oktaltext einer ganzen Zahl zurück.

Negative Zahlen verwenden das Zweierkomplement.

- `value` INTEGER: die Zahl
- Ergebnis `result` STRING: der Oktaltext

```c0de
CALL c0de/strings/OCT
  PARAMETER value 8 ;
  RESULT result octText
```
