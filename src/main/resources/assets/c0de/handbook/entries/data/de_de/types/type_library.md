```json
{
  "title": "TYPE_LIBRARY",
  "icon": "c0de:type_library_block",
  "category": "types",
  "ordinal": 11
}
```

TYPE\_LIBRARY steht für das Schlüsselwort TYPE: einen Typ einer Bibliothek einer anderen Mod, wie die Leinwand von c0de paint. Darauf folgt ein LABEL mit dem vollen Namen des Typs. Der LABEL-Dialog listet die Typen der Bibliotheken auf, die das Programm einbindet.

In einen Wert eines Bibliothekstyps kannst du nicht hineinsehen\; nur die Funktionen seiner Bibliothek arbeiten mit ihm. Er beginnt mit einem Wert, den seine Bibliothek wählt, zum Beispiel einer leeren Leinwand. Er lässt sich nicht mit einem Literal setzen, und es gibt keine Konstanten von ihm. `=` und `<>` vergleichen zwei Werte\; `⊕` schreibt eine kurze Beschreibung, zum Beispiel `CANVAS 20×10×8, 1600 voxels`. TO\_JSON und TO\_YAML können ihn nicht schreiben (TYPE\_MISMATCH).

Ein Prozess behält seine Werte über einen Neustart des Servers. Wird die Mod der Bibliothek entfernt, stürzt der Prozess beim Laden mit UNKNOWN\_LIBRARY ab. Ein Ergebnis eines Bibliothekstyps kommt als Gegenstand aus dem Auslöserblock, der die Beschreibung zeigt\; es kann nicht in einen anderen Prozess.

<recipe;c0de:type_library_block>

Stelle eine Typbasis und einen verzierten Krug zusammen her.

```c0de
INCLUDE c0de_paint/cpaint
VARIABLE picture TYPE c0de_paint/cpaint/CANVAS
```

`picture` enthält eine leere Leinwand von c0de paint.
