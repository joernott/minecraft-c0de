```json
{
  "title": "HEX",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 14
}
```

Gibt den Hexadezimaltext einer ganzen Zahl zurück.

Buchstaben sind groß. Negative Zahlen verwenden das Zweierkomplement, also wird -1 zu FFFFFFFFFFFFFFFF.

- `value` INTEGER: die Zahl
- Ergebnis `result` STRING: der Hexadezimaltext

```c0de
CALL c0de/strings/HEX
  PARAMETER value 255 ;
  RESULT result hexText
```
