```json
{
  "title": "RAISE",
  "icon": "minecraft:book",
  "category": "built_in_functions",
  "ordinal": 6
}
```

Returns your own error to the caller of the function.

The caller raises the error or stores it in the error label of its CALL line. `code` must be 1000 or higher, otherwise INVALID\_ARGUMENT is raised. The error gets the context and line of the CALL statement.

- `code` INTEGER: the error code, 1000 or higher
- `message` STRING: the error message (optional, default empty)

```c0de
CALL RAISE
  PARAMETER code 1001 ;
  PARAMETER message "too big" ;
```
