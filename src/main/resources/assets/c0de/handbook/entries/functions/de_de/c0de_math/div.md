```json
{
  "title": "DIV",
  "icon": "minecraft:book",
  "category": "c0de_math",
  "ordinal": 14
}
```

Teilt zwei ganze Zahlen und rundet in Richtung 0.

-7 geteilt durch 2 ist -3. Ein Teiler von 0 löst DIVISION\_BY\_ZERO aus.

- `dividend` INTEGER: die Zahl, die geteilt wird
- `divisor` INTEGER: die Zahl, durch die geteilt wird, nicht 0
- Ergebnis `result` INTEGER: der Quotient ohne Rest

```c0de
CALL c0de/math/DIV
  PARAMETER dividend items ;
  PARAMETER divisor 64 ;
  RESULT result stacks
```
