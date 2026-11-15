```json
{
  "title": "OPEN_VAULT",
  "icon": "minecraft:book",
  "category": "c0de_security",
  "ordinal": 4
}
```

Opens your persistent vault and adds its secrets to your vault.

Without `password`, your game asks you for it\; after a wrong one, you can try again. A wrong `password` raises WRONG\_PASSWORD. Without a persistent vault, the function raises VAULT\_NOT\_FOUND. An open vault stays open until you leave.

- `password` STRING: the password (optional, default: ask the player)
- no results

```c0de
CALL c0de/security/OPEN_VAULT
```
