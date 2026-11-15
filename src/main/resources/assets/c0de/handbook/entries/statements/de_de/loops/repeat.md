```json
{
  "title": "REPEAT",
  "icon": "c0de:repeat_block",
  "category": "loops",
  "ordinal": 3
}
```

REPEAT führt seinen Codeblock aus und prüft danach seine Bedingung.

Die REPEAT-Zeile enthält nur den REPEAT-Block. Die Bedingung kommt nach `END REPEAT`. Der Codeblock läuft erneut, solange die Bedingung wahr ist, und er läuft immer mindestens einmal.

<recipe;c0de:repeat_block>

<recipe;c0de:repeat_block_from_while_block>

Stelle eine Strukturbasis und einen Granitblock zusammen her, oder verwandle einen WHILE-Block in einen REPEAT-Block.

```c0de
REPEAT
BEGIN
  ASSIGN repeats repeats + 1 ;
END REPEAT n > 0 ;
```
