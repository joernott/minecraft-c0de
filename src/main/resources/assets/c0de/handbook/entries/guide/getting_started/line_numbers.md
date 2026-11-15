```json
{
  "title": "Line numbers",
  "icon": "c0de:wrench",
  "category": "getting_started",
  "ordinal": 4
}
```

At x=2, the floor shows the number of every line that has blocks. Its color tells you how the line is doing:

- faint: a NOOP or COMMENT line
- dark green: the line is correct
- dark yellow: correct so far, but the statement is not finished yet
- dark red: something is wrong

Dark yellow is normal while you build. A BEGIN without its END shows dark yellow until you place the END. The debugging tool tells you what is wrong with a red line.
