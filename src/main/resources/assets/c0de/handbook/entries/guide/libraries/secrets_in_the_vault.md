```json
{
  "title": "Secrets in the vault",
  "icon": "c0de:include_block",
  "category": "libraries",
  "ordinal": 15
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
  VARIABLE key STRING TAG(security: "sensitive")
  VARIABLE problem ERROR
  CALL c0de/security/HAS_VAULT
    RESULT result exists
  IF exists ;
  BEGIN
    CALL c0de/security/OPEN_VAULT problem
  END
  ELSE
  BEGIN
    CALL c0de/security/CREATE_VAULT problem
  END IF
  IF problem.code = VAULT_CANCELLED ;
  BEGIN
    CALL PRINT
      PARAMETER value "No password, no key." ;
    RETURN
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

The tag `security: "sensitive"` hides the key in the [debugging tool](^tools/debugging_tool). If the player cancels the password dialog, OPEN\_VAULT and CREATE\_VAULT raise VAULT\_CANCELLED, which the program catches. Another program then reads the key with `SECRET apiKey STRING`, after OPEN\_VAULT.

**Recipes**

<item;c0de:debug_tool>

<recipe;c0de:debug_tool>
