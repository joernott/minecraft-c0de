```json
{
  "title": "Making music",
  "icon": "c0de:include_block",
  "category": "libraries",
  "ordinal": 13
}
```

A short example: play a bell note at the [trigger block](^running_programs/three_trigger_blocks).

```c0de
CALL c0de/minecraft/SOUND
  PARAMETER note 12 ;
  PARAMETER instrument "bell" ;
```

`note` goes from 0 to 24, like the clicks on a note block. Without `instrument`, SOUND uses the harp.

**Recipes**

<item;c0de:trigger_block>

<recipe;c0de:trigger_block>
