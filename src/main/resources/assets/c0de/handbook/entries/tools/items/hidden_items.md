```json
{
  "title": "Hidden items",
  "icon": "minecraft:book",
  "category": "items",
  "ordinal": 3
}
```

Variable, constant and literal items can hide their value for good.

Set **Hide the value** to "Yes, for good" in the dialog of the item before you save it. From then on, the dialog and the tooltip show `***`. You can still rename a variable item. Only the trash can button removes the value, and with it the hiding. Hidden items don't stack with items that are not hidden.

A process can use the value like any other. The parameter it fills shows `***` in the debugging tool. Hiding protects against people who look at your screen, not against the server: for secrets that must stay safe, use the vault of c0de/security.

```c0de
FUNCTION login
  PARAMETER password STRING
BEGIN
  CALL PRINT
    PARAMETER value "Logged in" ;
END FUNCTION
```
