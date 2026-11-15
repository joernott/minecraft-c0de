```json
{
  "title": "EACH",
  "icon": "c0de:each_block",
  "category": "loops",
  "ordinal": 1
}
```

EACH geht durch ein Array oder einen Hash.

Danach folgen ein Name für die Schleifenvariable und ein LABEL mit dem Array oder Hash. Bei einem Array bekommt die Schleifenvariable der Reihe nach jedes Element. Bei einem Hash bekommt sie jeden Schlüssel in alphabetischer Reihenfolge.

Die Schleife arbeitet mit einer Kopie, wenn du also die Liste in der Schleife änderst, ändert sich die Schleife nicht. Du kannst die Schleifenvariable nicht selbst ändern.

<recipe;c0de:each_block>

<recipe;c0de:each_block_from_for_block>

Stelle eine Strukturbasis und einen Bruchsteinblock zusammen her, oder verwandle einen FOR-Block in einen EACH-Block.

```c0de
EACH key h
BEGIN
  ASSIGN keys keys ⊕ key ⊕ "=" ⊕ h[key] ⊕ " " ;
END EACH
```
