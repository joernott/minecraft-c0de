```json
{
  "title": "COMMENT",
  "icon": "c0de:comment_block",
  "category": "program_structure",
  "ordinal": 1
}
```

Ein COMMENT-Block enthält eine Notiz für Menschen.

Setze ihn ans Ende einer Zeile. Der Cursor ignoriert den COMMENT und jeden Block danach. Ein COMMENT vor Blöcken schaltet sie also ab.

Eine Zeile, die mit einem COMMENT beginnt, ist eine Kommentarzeile. Sie wird wie eine NOOP-Zeile übersprungen. Klicke den Block mit leerer Hand mit rechts an, um seinen Text zu schreiben.

<recipe;c0de:comment_block>

Lege einen Glasblock in die Mitte und acht NOOP-Blöcke darum herum. Du bekommst acht COMMENT-Blöcke.

Die Textschreibweise schreibt einen COMMENT-Block als `//` gefolgt von seinem Text.

```c0de
// Eine Kommentarzeile wird komplett übersprungen
VARIABLE time INTEGER 1 // ein Kommentar am Ende
```
