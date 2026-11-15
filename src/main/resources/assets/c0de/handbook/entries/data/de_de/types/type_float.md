```json
{
  "title": "TYPE_FLOAT",
  "icon": "c0de:type_float_block",
  "category": "types",
  "ordinal": 3
}
```

TYPE\_FLOAT steht für den Typ FLOAT: eine Zahl mit Nachkommastellen.

Ein neuer FLOAT enthält 0.0. Du darfst einen INTEGER in einem FLOAT speichern, aber nicht umgekehrt. Eine Division mit `/` ergibt immer einen FLOAT.

<recipe;c0de:type_float_block>

<recipe;c0de:type_float_block_from_type_integer_block>

Stelle eine Typbasis und einen roten Sandblock zusammen her, oder verwandle einen TYPE\_INTEGER-Block mit orangem Farbstoff in einen TYPE\_FLOAT-Block.

```c0de
VARIABLE half FLOAT
ASSIGN half 1 / 2 ;
```

Jetzt enthält `half` den Wert 0.5.
