```json
{
  "title": "HAS_KEY",
  "icon": "minecraft:book",
  "category": "built_in_functions",
  "ordinal": 1
}
```

Checks if a hash contains a key.

- `hash` HASH: the hash to look in
- `key` STRING: the key to look for
- result `result` BOOLEAN: true if the key exists

```c0de
CALL HAS_KEY
  PARAMETER hash stock ;
  PARAMETER key "gold" ;
  RESULT result hasGold
```
