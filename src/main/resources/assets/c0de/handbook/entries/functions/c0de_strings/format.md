```json
{
  "title": "FORMAT",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 12
}
```

Formats a value as text with a format string.

Conversions start with `%`, e.g. `%5d` (integer, 5 wide), `%.2f` (2 decimal places), `%s` (any value), `%tF` and `%tT` (date and time). A wrong type raises TYPE\_MISMATCH, a bad format string INVALID\_ARGUMENT.

All conversions use the same value, so one value can feed several of them: `"%tF %tT"` shows date and time, and `"%d = 0x%X"` turns 255 into `255 = 0xFF`. For two values, call FORMAT twice and join the texts with `⊕`.

`%f`, `%e` and `%g` take INTEGERs and FLOATs. `%d`, `%x` and `%o` only take INTEGERs, so round a FLOAT first with ROUND or INT of c0de/math.

The text is always English, whatever language the game or the server uses: `.` is the decimal point, `,` the thousands separator (`%,d` turns 1234567 into `1,234,567`), and the names of weekdays and months are English (`%tA` gives `Monday`). The `%t` codes use the time zone of the server.

- `value` BOOLEAN|INTEGER|FLOAT|CHAR|STRING|TIME: the value
- `format` STRING: the format string
- result `result` STRING: the formatted text

```c0de
CALL c0de/strings/FORMAT
  PARAMETER value now ;
  PARAMETER format "%tF %tT" ;
  RESULT result text
```
