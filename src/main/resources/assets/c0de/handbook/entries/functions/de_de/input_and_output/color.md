```json
{
  "title": "COLOR",
  "icon": "minecraft:book",
  "category": "input_and_output",
  "ordinal": 7
}
```

Legt die Farben für den Text fest, der ab jetzt ausgegeben wird.

Die Farben sind die 16 Farben von Minecraft-Text, von 0 (Schwarz) bis 15 (Weiß). Hintergrund 0 ist die Farbe des Bildschirms. Eine Zahl außerhalb von 0 bis 15 löst INVALID\_ARGUMENT aus.

Jedes Programm hat für jede Farbe eine Konstante: COLOR\_BLACK, COLOR\_DARK\_BLUE, COLOR\_DARK\_GREEN, COLOR\_DARK\_AQUA, COLOR\_DARK\_RED, COLOR\_DARK\_PURPLE, COLOR\_GOLD, COLOR\_GRAY, COLOR\_DARK\_GRAY, COLOR\_BLUE, COLOR\_GREEN, COLOR\_AQUA, COLOR\_RED, COLOR\_LIGHT\_PURPLE, COLOR\_YELLOW und COLOR\_WHITE.

- `foreground` INTEGER: die Farbe der Buchstaben, 0 bis 15
- `background` INTEGER: die Farbe hinter den Buchstaben, 0 bis 15 (optional, Standard 0)

```c0de
CALL COLOR
  PARAMETER foreground COLOR_YELLOW ;
  PARAMETER background COLOR_DARK_BLUE ;
```
