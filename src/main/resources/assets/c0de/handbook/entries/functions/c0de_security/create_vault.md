```json
{
  "title": "CREATE_VAULT",
  "icon": "minecraft:book",
  "category": "c0de_security",
  "ordinal": 3
}
```

Creates your persistent vault and opens it.

The vault is a KeePass file on the server, which only your game can open. Without `password`, your game asks you for a password and to repeat it. The password never leaves your game then. If you already have a persistent vault, the function raises VAULT\_ALREADY\_EXISTS. Closing the dialog raises VAULT\_CANCELLED.

- `password` STRING: the password (optional, default: ask the player)
- no results

```c0de
CALL c0de/security/CREATE_VAULT
```
