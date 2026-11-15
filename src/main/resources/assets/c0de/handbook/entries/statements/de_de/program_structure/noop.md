```json
{
  "title": "NOOP",
  "icon": "c0de:noop_block",
  "category": "program_structure",
  "ordinal": 0
}
```

NOOP bedeutet „no operation“ (keine Operation): Eine NOOP-Zeile tut nichts.

Verwende NOOP-Zeilen, um Teile deines Programms voneinander zu trennen. Eine NOOP-Zeile darf keinen anderen Block enthalten, nicht einmal einen COMMENT. Zeile 1 jedes Programms ist eine NOOP-Zeile, und jede FUNCTION braucht direkt darüber eine.

NOOP-Blöcke sind auch die Grundlage der meisten anderen Rezepte.

<recipe;c0de:noop_block>

Lege Eisenbarren in die vier Ecken und Stöcke an die vier Seiten, und lass die Mitte frei. Du bekommst 64 NOOP-Blöcke.

```c0de
PROGRAM first
NOOP
NOOP
FUNCTION answer
  RESULT result INTEGER
BEGIN
  ASSIGN result 42 ;
END FUNCTION
```
