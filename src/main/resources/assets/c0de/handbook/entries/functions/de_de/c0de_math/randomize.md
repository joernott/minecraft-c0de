```json
{
  "title": "RANDOMIZE",
  "icon": "minecraft:book",
  "category": "c0de_math",
  "ordinal": 16
}
```

Setzt den Startwert des Zufallszahlengenerators.

Derselbe Startwert ergibt immer dieselbe Folge von Zufallszahlen. Das betrifft nur den aktuellen Prozess.

- `seed` INTEGER: der Startwert

```c0de
CALL c0de/math/RANDOMIZE
  PARAMETER seed 42 ;
```
