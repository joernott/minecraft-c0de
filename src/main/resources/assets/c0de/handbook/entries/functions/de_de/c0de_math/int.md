```json
{
  "title": "INT",
  "icon": "minecraft:book",
  "category": "c0de_math",
  "ordinal": 10
}
```

Rundet eine Zahl auf eine ganze Zahl ab.

Aus -12.85 wird -13. Eine Zahl, die für einen INTEGER zu groß ist, löst NUMERIC\_OVERFLOW aus.

- `value` INTEGER|FLOAT: die Zahl
- Ergebnis `result` INTEGER: die größte ganze Zahl, die nicht größer als `value` ist

```c0de
CALL c0de/math/INT
  PARAMETER value height ;
  RESULT result blocks
```
