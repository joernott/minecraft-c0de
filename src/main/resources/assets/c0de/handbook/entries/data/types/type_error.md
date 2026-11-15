```json
{
  "title": "TYPE_ERROR",
  "icon": "c0de:type_error_block",
  "category": "types",
  "ordinal": 10
}
```

TYPE\_ERROR stands for the type ERROR: an error report.

An error has four elements: `code`, `message`, `context` (the function) and `line`. Code 0 means no error. Your own error codes must be 1000 or higher.

A CALL with an ERROR variable writes into it every time. After a CALL without error, its `code` is 0 again. The names of the errors are constants, also those of the libraries of c0de, like PERMISSION\_DENIED or CHUNK\_NOT\_LOADED. Errors of other mods are only constants if the mod adds them.

<recipe;c0de:type_error_block>

Craft a type base and a campfire together.

```c0de
VARIABLE problem ERROR
CALL fail problem
ASSIGN code problem.code ;
```

The CALL catches the error of `fail` in `problem`.
