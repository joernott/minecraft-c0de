```json
{
  "title": "SWAP",
  "icon": "minecraft:book",
  "category": "built_in_functions",
  "ordinal": 5
}
```

Vertauscht zwei Werte.

Beide Werte müssen denselben Typ haben, sonst wird TYPE\_MISMATCH ausgelöst. Um zwei Variablen zu vertauschen, nimm sie als Parameter und als Ziele der Ergebnisse.

- `a` beliebiger Typ: der erste Wert
- `b` der Typ von `a`: der zweite Wert
- Ergebnis `a`: der Wert von `b`
- Ergebnis `b`: der Wert von `a`

```c0de
CALL SWAP
  PARAMETER a left ;
  PARAMETER b right ;
  RESULT a left
  RESULT b right
```
