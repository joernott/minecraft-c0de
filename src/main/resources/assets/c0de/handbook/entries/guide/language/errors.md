```json
{
  "title": "Errors",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 20
}
```

When something goes wrong, like a division by zero, the function stops and **raises** an error. The error goes to the caller, and then up to its caller.

If the error reaches the entry function, the process **crashes**. Mistakes in the program itself, like a wrong indentation, crash it at once.

To catch an error, add a variable of type ERROR to the CALL line. The caller then goes on, and you can check the error's `code`, `message`, `context` and `line`.

Every CALL with such a variable writes into it, also when nothing went wrong: then its `code` is 0 again and its `message` is empty. So check it right after the CALL.

The names of the errors are constants, like `DIVISION_BY_ZERO`. This includes the errors of the libraries of c0de, like PERMISSION\_DENIED, CHUNK\_NOT\_LOADED, INVENTORY\_FULL and ITEM\_MISSING. Errors of libraries from other mods are only constants if that mod adds them. Otherwise compare `code` with the number from their handbook.

```c0de
VARIABLE problem ERROR
CALL fail problem
ASSIGN code problem.code ;
```

**Recipes**

<item;c0de:type_error_block>

<recipe;c0de:type_error_block>

<item;c0de:call_block>

<recipe;c0de:call_block>
