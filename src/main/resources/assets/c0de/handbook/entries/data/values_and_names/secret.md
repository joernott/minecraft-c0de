```json
{
  "title": "SECRET",
  "icon": "c0de:secret_block",
  "category": "values_and_names",
  "ordinal": 4
}
```

A SECRET line names a secret from your vault: a password or key that a program needs, but nobody should see.

It looks like a CONSTANT line without a value: `SECRET apiKey STRING`. The type is BOOLEAN, INTEGER, FLOAT, CHAR, STRING or TIME. When the cursor reaches the line, it takes the value of the secret with that name from the vault of the player who started the program. You use it like a constant. The debugging tool shows `***` instead of its value.

The vault only works for the owner of the sheet who starts the program, e.g. with a button. Without a vault, the line raises VAULT\_UNAVAILABLE. If the vault has no secret of that name, it raises SECRET\_NOT\_FOUND. The functions of c0de/security put secrets into the vault.

<recipe;c0de:secret_block>

<recipe;c0de:constant_block_from_secret_block>

Craft a CONSTANT block and an iron ingot, or turn a SECRET block back into a CONSTANT block.

```c0de
SECRET apiKey STRING
VARIABLE url STRING TAG(security: "sensitive")
ASSIGN url "https://example.org/weather?key=" ⊕ apiKey ;
```
