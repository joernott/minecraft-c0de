```json
{
  "title": "Secrets in the vault",
  "icon": "c0de:include_block",
  "category": "libraries",
  "ordinal": 22
}
```

Every player has a vault while they are online. SET\_SECRET puts a secret into it, and a SECRET line reads it, like a constant. Only the owner of the sheet can use the vault, and only when they start the program themselves, e.g. with a button.

A persistent vault keeps secrets when you leave. CREATE\_VAULT makes one, OPEN\_VAULT opens it. Your game asks for the password, and only your game can open the file\; the server keeps it, but can't read it.

```c0de
PROGRAM keys
NOOP
INCLUDE c0de/security
NOOP
FUNCTION setup
BEGIN
  VARIABLE exists BOOLEAN
  VARIABLE key STRING
  CALL c0de/security/HAS_VAULT
    RESULT result exists
  IF exists ;
  BEGIN
    CALL c0de/security/OPEN_VAULT
  END
  ELSE
  BEGIN
    CALL c0de/security/CREATE_VAULT
  END IF
  CALL INPUT
    PARAMETER variable key ;
    PARAMETER hide TRUE ;
  CALL c0de/security/SET_SECRET
    PARAMETER name "apiKey" ;
    PARAMETER value key ;
    PARAMETER persistent TRUE ;
END FUNCTION
```

Another program then reads the key with `SECRET apiKey STRING`, after OPEN\_VAULT.
