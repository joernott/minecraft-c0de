```json
{
  "title": "EACH-Schleifen",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 16
}
```

EACH geht durch ein Array oder einen Hash. Bei einem Array bekommt die Schleifenvariable jedes Element. Bei einem Hash bekommt sie jeden Schlüssel in alphabetischer Reihenfolge.

```c0de
EACH key h
BEGIN
  ASSIGN keys keys ⊕ key ⊕ "=" ⊕ h[key] ⊕ " " ;
END EACH
```

**Rezepte**

<item;c0de:each_block>

<recipe;c0de:each_block>
