```json
{
  "title": "REMOVE",
  "icon": "minecraft:book",
  "category": "built_in_functions",
  "ordinal": 2
}
```

Removes one element from an array or a hash.

Use `index` for an array and `key` for a hash. The following array elements move down by one. A missing index raises INDEX\_OUT\_OF\_BOUNDS, a missing key raises UNKNOWN\_KEY. Leaving out `index` or `key` raises INVALID\_ARGUMENT.

- `container` ARRAY|HASH: the array or hash
- `index` INTEGER: position of the element to remove (optional, needed for arrays)
- `key` STRING: key of the element to remove (optional, needed for hashes)
- result `result` like `container`: the array or hash without the element

```c0de
CALL REMOVE
  PARAMETER container list ;
  PARAMETER index 1 ;
  RESULT result list
```
