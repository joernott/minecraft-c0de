```json
{
  "title": "NOOP und COMMENT",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 2
}
```

NOOP bedeutet „no operation“ (keine Operation). Eine NOOP-Zeile tut nichts. Verwende sie, um Teile deines Programms zu trennen. Eine NOOP-Zeile darf keinen anderen Block enthalten. Jede FUNCTION braucht direkt darüber eine NOOP-Zeile.

Ein COMMENT-Block enthält eine Notiz für Menschen. Setze ihn ans Ende einer Zeile. Der Cursor ignoriert ihn und jeden Block danach. Ein COMMENT vor Blöcken schaltet sie also ab.

Eine Zeile, die mit einem COMMENT-Block beginnt, ist eine Kommentarzeile. Sie wird wie eine NOOP-Zeile übersprungen.

```c0de
// Eine Kommentarzeile wird ganz übersprungen
VARIABLE time INTEGER 1 // ein Kommentar am Ende
ASSIGN count time ; COMMENT("disabled") + 100 ;
```

In der letzten Zeile ist `COMMENT("disabled")` ein COMMENT-Block mit dem Text "disabled". Das `+ 100 ;` danach ist abgeschaltet.
