# Security and the c0de/security library

[c0de concept](../concept.md) › Security and the c0de/security library

Programs use passwords and API keys, e.g. for the web services of [c0de net](https://github.com/joernott/minecraft-c0de-net). These values shouldn't be on the screen while a player types them, in the dialog of an item that somebody looks at, in the debug output of a cursor or in the program itself. c0de has four tools for this:

  - **hidden input**: INPUT shows `*` instead of the typed characters on laptops and tablets,
  - **hidden items**: variable, constant and literal items whose value can't be seen any more,
  - **sensitive values**: the tag `security: sensitive` replaces a value with `***` in the debug output,
  - **the vault**: every player has a vault of secrets, which programs read with the SECRET statement. Secrets can be kept in a KeePass file that only the client of the player can decrypt.

## What they protect against

Hidden input, hidden items and sensitive values protect against looking over the shoulder: other players, screenshots and videos don't show the values. They don't protect against the server: the values are in the memory of the server while a program uses them, and the value of a hidden item is part of the item (like every item property, a player with tools that read item data can see it). Secrets that must not be stored on the server in readable form belong into the persistent vault: the server only keeps the encrypted file.

Administrators (operators with permission level 2 or higher) can change every sheet ([Sheet owners](01-dimension.md#sheet-owners)) and so every program that reads secrets. A player who uses the vault trusts the administrators of the server.

## Hidden input

[INPUT](19-input-output.md#input) has an optional parameter `hide` (BOOLEAN, default FALSE). With `hide` TRUE, the input line of a bound laptop or tablet shows a `*` for every typed character, and the typed line is printed on the screen of its terminal as `***`. The value itself is read as without `hide`. Values from I/O blocks are not affected.

```
CALL INPUT
    PARAMETER variable password ;
    PARAMETER hide TRUE ;
```

## Hidden items

The dialogs of [variable](14-items-and-tools.md#variable-item), [constant](14-items-and-tools.md#constant-item) and [literal items](19-input-output.md#literal-item) have the choice **Hide the value**: **No** (the default) or **Yes, for good**. Once an item with a value is saved with **Yes, for good**, its value is hidden:
  - the dialog shows `***` in a read-only value field, and the checkbox is checked and disabled,
  - the tooltip of the item shows `***` instead of the value,
  - the item keeps its name and type; only the name of a variable item can still be changed, a hidden constant or literal item can only be emptied.

Hiding can't be undone: only the trash can button of the dialog, which empties all fields (and makes a variable or constant item uninitialized, a literal item empty), removes the value together with **Hide**. Items with a hidden value don't stack with items without one.

A hidden item passes its value to a process like any other item. The parameter it fills is [sensitive](#sensitive-values). [WRITE](19-input-output.md#write) of a sensitive value puts a hidden variable item into the I/O block. INPUT from a hidden item works as from any other item. The results of a process are new variable items that are not hidden; a program that returns a secret to the trigger block shows it in the item.

LITERAL blocks can't be hidden: the code of a program stays readable on its sheet. Values that must not be seen belong into hidden items or into the vault.

## Sensitive values

A value is *sensitive* if it is
  - the value of a variable or constant whose line has the tag `security: sensitive`,
  - the value of an element of a struct whose declaration has the tag `security: sensitive` for that element,
  - the value of a [SECRET](#secrets-in-programs), or
  - the value of a parameter of the entry function that comes from a hidden item.

The debug output shows `***` instead of a sensitive value:
  - the values of the stack in the dialog of a cursor and in the chat when a cursor is inspected with the debugging tool ([The cursor](15-cursor.md)),
  - the report of the [program runner](../runner.md) (the results and the variables of an error).

An array, hash or struct that contains a sensitive element is shown with `***` in place of that element only. A sensitive value of a variable or constant of type ARRAY, HASH or STRUCT is shown as `***` as a whole.

Sensitivity belongs to the name, not to the value: assigning a sensitive value to a variable without the tag makes it visible in the debug output of that variable. The programs themselves can use sensitive values without restrictions: PRINT prints them, TO_JSON converts them, c0de net sends them.

### The security tag

The tag `security: sensitive` is a [TAG block](04-data.md#tags) with the language `security` and the name `sensitive`, the only name of this language. It MAY be placed wherever a JSON or YAML tag can be placed: at the end of a VARIABLE or CONSTANT line and at the end of an element line of a STRUCT declaration. As for the other languages, a line has at most one tag of the language `security`. It doesn't change JSON or YAML.

```
STRUCT login
BEGIN
    user STRING TAG(json: "user")
    password STRING TAG(json: "password") TAG(security: "sensitive")
END STRUCT
VARIABLE token STRING TAG(security: "sensitive")
```

## The vault

Every player has a vault: the secrets of the player while they are online. The vault is created empty when the player joins the server and vanishes with all its secrets when they leave (also when the server stops). It lives in the memory of the server.

A secret has
  - a **name**, a valid name ([Naming with labels](02-language-basics.md#naming-with-labels)) that is unique in the vault,
  - a **type**: BOOLEAN, INTEGER, FLOAT, CHAR, STRING or TIME,
  - a **value** of that type, and
  - the property **persistent**: a persistent secret is also stored in the persistent vault of the player.

### Which vault a process uses

A process uses the vault of its activator, the player who started it (`PID.activator`, see [Built-in constants](04-data.md#built-in-constants)). The vault is only available to a process if
  - the process has an activator who is a player and online, and
  - this player is the owner of the sheet of the program.

The second rule keeps a program of another player from reading the secrets of whoever presses its button. Processes of timer blocks and processes started by redstone without an activator have no vault. A SECRET statement or a function of c0de/security in a process without a vault raises VAULT_UNAVAILABLE.

### The persistent vault

The persistent vault is a file in the KeePass format KDBX 4 per player: `<world>/data/c0de_vaults/<player-uuid>.kdbx`. The server stores the file but never decrypts it: it doesn't have the password, and the file is read and written only on the client of its player, with the library [kdbx](https://github.com/hudeany/kdbx) (`de.soderer:kdbx`, with Bouncy Castle). A player only gets their own file.

  - **Creating**: [CREATE_VAULT](#functions) makes the client create an empty database with the password (KDBX 4, Argon2id, AES-256) and send the encrypted file to the server, which stores it. The persistent vault is open afterwards.
  - **Opening**: [OPEN_VAULT](#functions) sends the file to the client, which decrypts it and sends the persistent secrets to the server, where they are added to the vault (a secret of the same name in the vault is replaced). The client keeps the decrypted database and its key in memory while the player is online. The persistent vault stays open until the player leaves.
  - **Changing**: if the persistent vault is open, SET_SECRET and DELETE_SECRET of persistent secrets send the change to the client, which changes its database, encrypts it again and sends the new file to the server. The cursor waits on the CALL line until the server has stored the file.
  - **Format**: the secrets are entries of the group `c0de` of the database: the title is the name, the password field the value in the notation of a LITERAL block, and the custom field `c0de-type` the type. The file can be opened with KeePass and other applications that read KDBX 4.

A password typed in the dialog of the client never reaches the server. A password passed to CREATE_VAULT or OPEN_VAULT as a parameter is sent from the server to the client: it is part of the program, which the server knows anyway. Such a program SHOULD take the password from a hidden item or a hidden INPUT, not from a LITERAL block.

### Password dialog

CREATE_VAULT and OPEN_VAULT without a password open a dialog on the client of the activator. The cursor waits on the CALL line until the player answers it.
  - CREATE_VAULT: two password fields, **Password** and **Repeat**; OK is only enabled if both are equal and not empty.
  - OPEN_VAULT: one password field. A wrong password shows a message in the dialog, which stays open for another try.

Both have a Cancel button: closing the dialog raises VAULT_CANCELLED. If the player leaves while the dialog is open, the call raises VAULT_UNAVAILABLE.

## Secrets in programs

A SECRET statement declares a name for a secret of the vault. It consists of a SECRET keyword directly followed by a valid, unique LABEL and a type: BOOLEAN, INTEGER, FLOAT, CHAR, STRING or TIME. It MAY end with a COMMENT block, but not with tags or a literal. A wrong SECRET line is a SYNTAX_ERROR_CONSTANT, as a secret is a constant. A secret is treated like a [constant](04-data.md#constant): it can be used wherever a constant can, it MUST NOT be the target of an ASSIGN statement, a CALL result or a loop, and it is visible in the scope in which it is declared.

```
SECRET apiKey STRING
VARIABLE url STRING TAG(security: "sensitive")
ASSIGN url "https://example.org/weather?key=" ⊕ apiKey ;
```

The value is read from the vault of the process when the cursor executes the SECRET line. If the vault has no secret of that name, the statement raises SECRET_NOT_FOUND; if the secret has another type, TYPE_MISMATCH. Later changes of the secret in the vault don't change the value in the running process. The value of a SECRET is [sensitive](#sensitive-values). During validation, a SECRET has the default value of its type. In the example, the URL with the key is sensitive too, because its variable has the security tag.

### Minecraft block

A SECRET block looks like a CONSTANT block (a blue barrel, as high as a slab) with a padlock on its front. The [line number](01-dimension.md#line-numbers) shows whether the statement is valid. It can be crafted shapelessly from a CONSTANT block and an iron ingot, and turned back into a CONSTANT block shapelessly.

## Functions

The library `c0de/security` MUST be included with `INCLUDE c0de/security`. All its functions use the vault of the process (see [Which vault a process uses](#which-vault-a-process-uses)).

| Function | Parameters | Results | Description |
|---|---|---|---|
| SET_SECRET | name STRING, value (BOOLEAN, INTEGER, FLOAT, CHAR, STRING or TIME), persistent BOOLEAN (default FALSE) | – | Creates or replaces the secret `name` with the type and value of `value`. With `persistent` TRUE, it is also written to the persistent vault, which MUST be open (otherwise VAULT_NOT_OPEN). With `persistent` FALSE, a persistent secret of that name is removed from the persistent vault (which then MUST be open) and only kept in the vault. |
| DELETE_SECRET | name STRING | – | Removes the secret `name` from the vault, and from the persistent vault if it is persistent (which then MUST be open). Raises SECRET_NOT_FOUND if the vault has no secret of that name. |
| CREATE_VAULT | password STRING (default "") | – | Creates the persistent vault of the player and opens it. Without a password or with an empty one, the player is asked in the [password dialog](#password-dialog). Raises VAULT_ALREADY_EXISTS if the player already has one. |
| OPEN_VAULT | password STRING (default "") | – | Opens the persistent vault of the player and adds its secrets to the vault. Without a password or with an empty one, the player is asked in the password dialog. Raises VAULT_NOT_FOUND if the player has none, WRONG_PASSWORD if the given password is wrong. Opening an open vault does nothing. |
| HAS_SECRET | name STRING | result BOOLEAN | TRUE if the vault has a secret `name`. |
| HAS_VAULT | – | result BOOLEAN | TRUE if the player has a persistent vault. |
| IS_VAULT_OPEN | – | result BOOLEAN | TRUE if the persistent vault of the player is open. |

`name` MUST be a valid name (otherwise INVALID_ARGUMENT), and the text of `value` MUST NOT be longer than 2000 characters (INVALID_ARGUMENT). A player can keep at most 128 secrets; a further one raises INVALID_ARGUMENT.

The usual first program creates the persistent vault once; later programs open it and set secrets:

```
PROGRAM keys
NOOP
INCLUDE c0de/security
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

## Errors

| Code | Name | Description |
|---|---|---|
| 314 | VAULT_UNAVAILABLE | The process has no vault: no activator, the activator is not online or doesn't own the sheet, or the player left while the cursor waited. |
| 315 | SECRET_NOT_FOUND | The vault has no secret of that name (SECRET, DELETE_SECRET). |
| 316 | VAULT_NOT_OPEN | A persistent secret is changed, but the persistent vault is not open. |
| 317 | VAULT_NOT_FOUND | OPEN_VAULT: the player has no persistent vault. |
| 318 | VAULT_ALREADY_EXISTS | CREATE_VAULT: the player already has a persistent vault. |
| 319 | WRONG_PASSWORD | OPEN_VAULT: the given password doesn't decrypt the persistent vault. |
| 320 | VAULT_CANCELLED | The player closed the password dialog. |
| 321 | VAULT_FAILED | The client couldn't read or write the persistent vault (e.g. a damaged file); the message says why. |

All of them can be handled by the program like the other errors that are raised (see [Error codes](16-error-codes.md)).

## The program runner

The [program runner](../runner.md) has no players. Its options file MAY contain a vault for the process:

```yaml
vault:
  secrets:
    apiKey: {type: STRING, value: abc}
  persistent:
    password: secret
    open: false
    secrets:
      token: {type: STRING, value: xyz}
```

Without `vault`, the process has no vault, and SECRET and c0de/security raise VAULT_UNAVAILABLE. With `vault`, the functions work on the secrets of the options in memory; without `persistent`, the player has no persistent vault. CREATE_VAULT and OPEN_VAULT compare their password with `persistent.password` (a call without a password raises VAULT_CANCELLED, as there is nobody to ask), and OPEN_VAULT adds `persistent.secrets` to the vault. The runner never reads or writes KDBX files.
