```json
{
  "title": "SOUND",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 3
}
```

Plays a note at the trigger block like a note block.

`note` is 0 to 24, like the clicks on a note block. A wrong note or instrument raises INVALID\_ARGUMENT. Capitals don't matter in the name of the instrument.

The instruments, with the block under a note block that plays them: harp (other blocks), basedrum (stone), snare (sand), hat (glass), bass (wood), flute (clay), bell (gold block), guitar (wool), chime (packed ice), xylophone (bone block), iron\_xylophone (iron block), cow\_bell (soul sand), didgeridoo (pumpkin), bit (emerald block), banjo (hay bale), pling (glowstone).

The heads of mobs play their sounds: zombie, skeleton, creeper, dragon, wither\_skeleton and piglin. custom\_head only clicks. Since Minecraft 26.1, there are also trumpet, trumpet\_exposed, trumpet\_weathered and trumpet\_oxidized.

- `note` INTEGER: the note, 0 to 24
- `instrument` STRING: the instrument (optional, default "harp")

```c0de
CALL c0de/minecraft/SOUND
  PARAMETER note 12 ;
  PARAMETER instrument "bell" ;
```
