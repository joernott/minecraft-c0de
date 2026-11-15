```json
{
  "title": "Display block",
  "icon": "c0de:display_block",
  "category": "screens",
  "ordinal": 0
}
```

The display block shows the text of PRINT on its front.

One display block has 16 columns and 8 rows. Display blocks next to each other in a flat rectangle, all facing the same way, form a display wall with one big screen. A wall of 4 × 2 blocks has 64 columns and 16 rows. A wall can stand upright or lie flat.

Right-click a display block with an empty hand to open its dialog. Click the Program and Function buttons until they show your program and function. Then click ✔ to bind the block. Binding one block binds the whole wall. A new block that joins a wall takes its binding.

The dialog also has a camouflage slot. Put a full block into it, and the sides of the display block take on its look. Take it out to remove the camouflage. Each block of a wall has its own camouflage. Left-click a display block with the wrench to rotate it.

<recipe;c0de:display_block>

Seven iron nuggets around a glowstone dust in the middle, with a redstone dust left of it, give eight display blocks.

Build a wall of 2 × 1 display blocks and bind it to the function `welcome`. Start the function with a trigger block. The wall shows "Welcome\!" in yellow.

```c0de
PROGRAM sign
NOOP
NOOP
FUNCTION welcome
BEGIN
  CALL CLS
  CALL COLOR
    PARAMETER foreground 14 ;
  CALL PRINT
    PARAMETER value "Welcome!" ;
END FUNCTION
```
