```json
{
  "title": "DELETE_SECRET",
  "icon": "minecraft:book",
  "category": "c0de_security",
  "ordinal": 1
}
```

Removes a secret from your vault.

A persistent secret is also removed from the file of your persistent vault, which must be open. A secret that doesn't exist raises SECRET\_NOT\_FOUND.

- `name` STRING: the name of the secret
- no results

```c0de
CALL c0de/security/DELETE_SECRET
  PARAMETER name "apiKey" ;
```
