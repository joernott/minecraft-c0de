```json
{
  "title": "INSERT",
  "icon": "minecraft:book",
  "category": "built_in_functions",
  "ordinal": 3
}
```

Inserts a value into an array.

The following elements move up by one. `index` must be between 0 and the length of the array, otherwise INDEX\_OUT\_OF\_BOUNDS is raised. Use the length to add the value at the end.

- `array` ARRAY: the array
- `index` INTEGER: the position of the new element
- `value` the type of the elements: the value to insert
- result `result` like `array`: the array with the new element

```c0de
CALL INSERT
  PARAMETER array list ;
  PARAMETER index 0 ;
  PARAMETER value 42 ;
  RESULT result list
```
