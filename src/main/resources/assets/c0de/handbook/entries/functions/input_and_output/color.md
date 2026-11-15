```json
{
  "title": "COLOR",
  "icon": "minecraft:book",
  "category": "input_and_output",
  "ordinal": 7
}
```

Sets the colors of the text printed from now on.

The colors are the 16 colors of Minecraft text, from 0 (black) to 15 (white). Background 0 is the color of the screen. A number outside 0 to 15 raises INVALID\_ARGUMENT.

Every program has a constant for each color: COLOR\_BLACK, COLOR\_DARK\_BLUE, COLOR\_DARK\_GREEN, COLOR\_DARK\_AQUA, COLOR\_DARK\_RED, COLOR\_DARK\_PURPLE, COLOR\_GOLD, COLOR\_GRAY, COLOR\_DARK\_GRAY, COLOR\_BLUE, COLOR\_GREEN, COLOR\_AQUA, COLOR\_RED, COLOR\_LIGHT\_PURPLE, COLOR\_YELLOW and COLOR\_WHITE.

- `foreground` INTEGER: the color of the letters, 0 to 15
- `background` INTEGER: the color behind the letters, 0 to 15 (optional, default 0)

```c0de
CALL COLOR
  PARAMETER foreground COLOR_YELLOW ;
  PARAMETER background COLOR_DARK_BLUE ;
```
