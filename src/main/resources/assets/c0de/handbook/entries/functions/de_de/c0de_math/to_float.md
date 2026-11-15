```json
{
  "title": "TO_FLOAT",
  "icon": "minecraft:book",
  "category": "c0de_math",
  "ordinal": 13
}
```

Wandelt eine Zahl in einen FLOAT um.

Du brauchst sie selten. Ein INTEGER kommt von selbst in eine FLOAT-Variable, einen FLOAT-Parameter oder ein FLOAT-Ergebnis, `/` ergibt immer ein FLOAT, und die Mathematikfunktionen und `%f` von FORMAT nehmen auch INTEGERs.

Verwende sie, wenn eine Rechnung mit INTEGERs zu groß wird: `+`, `*` und `^` von zwei INTEGERs lösen über 9223372036854775807 NUMERIC\_OVERFLOW aus, mit einem FLOAT nicht. Oder wenn ein Text ein FLOAT zeigen soll: `⊕` schreibt den INTEGER 8 als `8` und das FLOAT als `8.0`.

- `value` INTEGER|FLOAT: die Zahl
- Ergebnis `result` FLOAT: dieselbe Zahl als FLOAT

```c0de
CALL c0de/math/TO_FLOAT
  PARAMETER value count ;
  RESULT result amount
```
