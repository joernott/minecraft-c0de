```json
{
  "title": "SET_SECRET",
  "icon": "minecraft:book",
  "category": "c0de_security",
  "ordinal": 0
}
```

Puts a secret into your vault.

A secret with the same name is replaced. The type of the secret is the type of `value`. A vault holds up to 128 secrets of up to 2000 characters. With `persistent` TRUE, the secret is also stored in your persistent vault, which must be open (VAULT\_NOT\_OPEN otherwise). The cursor waits while your game encrypts the file.

- `name` STRING: the name of the secret
- `value` BOOLEAN, INTEGER, FLOAT, CHAR, STRING or TIME: the value
- `persistent` BOOLEAN: also store it in the persistent vault (optional, default FALSE)
- no results

```c0de
CALL c0de/security/SET_SECRET
  PARAMETER name "apiKey" ;
  PARAMETER value key ;
  PARAMETER persistent TRUE ;
```
