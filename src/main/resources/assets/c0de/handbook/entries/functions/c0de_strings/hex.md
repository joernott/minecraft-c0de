```json
{
  "title": "HEX",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 14
}
```

Returns the hexadecimal text of an integer.

Letters are uppercase. Negative numbers use two's complement, so -1 becomes FFFFFFFFFFFFFFFF.

- `value` INTEGER: the number
- result `result` STRING: the hexadecimal text

```c0de
CALL c0de/strings/HEX
  PARAMETER value 255 ;
  RESULT result hexText
```
