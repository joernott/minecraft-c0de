```json
{
  "title": "EACH loops",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 16
}
```

EACH goes through an array or a hash. For an array, the loop variable gets each element. For a hash, it gets each key in alphabetical order.

```c0de
EACH key h
BEGIN
  ASSIGN keys keys ⊕ key ⊕ "=" ⊕ h[key] ⊕ " " ;
END EACH
```
