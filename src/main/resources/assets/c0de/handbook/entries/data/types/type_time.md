```json
{
  "title": "TYPE_TIME",
  "icon": "c0de:type_time_block",
  "category": "types",
  "ordinal": 9
}
```

TYPE\_TIME stands for the type TIME: a point in time.

A new TIME holds the start of 1970 (UTC). You can add or subtract milliseconds as an INTEGER. Two times subtracted give the difference in milliseconds.

Times can be compared with `=`, `<`, `>` and the others: the earlier time is the smaller one. `⊕` writes a TIME as ISO text in UTC with milliseconds, like `2026-10-19T12:05:09.250Z`. FORMAT of c0de/strings writes it in the time zone of the server.

<recipe;c0de:type_time_block>

Craft a type base and a clock together.

```c0de
VARIABLE start TIME @2026-10-19T12:05:09Z
VARIABLE later TIME
ASSIGN later start + 60000 ;
```

Now `later` is one minute after `start`.
