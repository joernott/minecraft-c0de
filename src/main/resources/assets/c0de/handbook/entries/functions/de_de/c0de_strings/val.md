```json
{
  "title": "VAL",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 13
}
```

Wandelt einen Text in eine Zahl um.

`isInteger` ist wahr, wenn der Text eine ganze Zahl ist. Nimm dann FIX, um den Wert in einen INTEGER zu verwandeln. Ein Text, der keine Zahl ist, löst INVALID\_ARGUMENT aus.

- `text` STRING: der Text, z. B. "42" oder "3.5"
- Ergebnis `value` FLOAT: die Zahl
- Ergebnis `isInteger` BOOLEAN: wahr, wenn der Text eine ganze Zahl ist

```c0de
CALL c0de/strings/VAL
  PARAMETER text input ;
  RESULT value number
  RESULT isInteger whole
```
