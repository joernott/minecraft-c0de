```json
{
  "title": "HAS_SECRET",
  "icon": "minecraft:book",
  "category": "c0de_security",
  "ordinal": 2
}
```

Checks whether your vault has a secret.

- `name` STRING: the name of the secret
- result `result` BOOLEAN: TRUE if the secret exists

```c0de
CALL c0de/security/HAS_SECRET
  PARAMETER name "apiKey" ;
  RESULT result found
```
