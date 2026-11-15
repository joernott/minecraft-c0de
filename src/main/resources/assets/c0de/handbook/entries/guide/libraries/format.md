```json
{
  "title": "FORMAT",
  "icon": "c0de:include_block",
  "category": "libraries",
  "ordinal": 7
}
```

FORMAT turns a value into text with a format string. `%d` is a whole number, `%f` a decimal number and `%s` any value. A number between `%` and the letter sets the width.

- `[%4d]` turns 42 into `[  42]`
- `%05d` turns 42 into `00042`
- `%.2f` turns 3.14159 into `3.14`
- `%,d` turns 1234567 into `1,234,567`

For TIME values, `%tF` gives the date and `%tT` the time, like `2026-10-19` and `14:05:09`. Many more codes exist, such as `%tA` for the name of the weekday.

All codes use the same value, so `"%tF %tT"` shows date and time at once. `%f` also takes an INTEGER, but `%d` doesn't take a FLOAT. The text is always English: `.` and `,` in numbers, English names of weekdays and months, in the time zone of the server.
