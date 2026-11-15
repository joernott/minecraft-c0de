```json
{
  "title": "MID",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 2
}
```

Gibt einen Teil eines Texts zurück.

Positionen beginnen bei 0. Kurz vor dem Ende bekommst du weniger Zeichen. Ein Start hinter dem Ende ergibt einen leeren Text. Ein negativer Start oder eine negative Länge löst INVALID\_ARGUMENT aus.

- `text` STRING: der Text
- `start` INTEGER: die Position des ersten Zeichens
- `length` INTEGER: die Anzahl der Zeichen (optional, Standard der Rest des Texts)
- Ergebnis `result` STRING: der Teil des Texts

```c0de
CALL c0de/strings/MID
  PARAMETER text name ;
  PARAMETER start 2 ;
  PARAMETER length 3 ;
  RESULT result part
```
