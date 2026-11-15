```json
{
  "title": "The sheet layout",
  "icon": "c0de:wrench",
  "category": "getting_started",
  "ordinal": 3
}
```

A program starts at the north end of a sheet and runs south. Each row of blocks across the sheet is a **line**. The first line is line 0.

Inside a sheet, x counts from the west edge. Language blocks go between x=5 and x=28, so a line holds at most 24 blocks. That keeps lines short and easy to read.

At x=4 there is a gray rail: the **cursor rail**. When a program runs, a glowing cursor moves along it and shows which line is working right now.
