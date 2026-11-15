```json
{
  "title": "EACH",
  "icon": "c0de:each_block",
  "category": "loops",
  "ordinal": 1
}
```

EACH goes through an array or a hash.

A name for the loop variable and a LABEL with the array or hash follow. For an array, the loop variable gets each element in order. For a hash, it gets each key in alphabetical order.

The loop works on a copy, so changing the list inside the loop doesn't change the loop. You can't change the loop variable yourself.

<recipe;c0de:each_block>

<recipe;c0de:each_block_from_for_block>

Craft a structural base and a cobblestone block, or turn a FOR block into an EACH block.

```c0de
EACH key h
BEGIN
  ASSIGN keys keys ⊕ key ⊕ "=" ⊕ h[key] ⊕ " " ;
END EACH
```
