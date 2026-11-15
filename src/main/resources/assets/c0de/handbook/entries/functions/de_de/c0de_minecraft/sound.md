```json
{
  "title": "SOUND",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 3
}
```

Spielt am Auslöserblock einen Ton wie ein Notenblock.

`note` ist 0 bis 24, wie die Klicks auf einem Notenblock. Ein falscher Ton oder ein falsches Instrument löst INVALID\_ARGUMENT aus. Groß- und Kleinschreibung im Namen des Instruments sind egal.

Die Instrumente, mit dem Block unter einem Notenblock, der sie spielt: harp (andere Blöcke), basedrum (Stein), snare (Sand), hat (Glas), bass (Holz), flute (Ton), bell (Goldblock), guitar (Wolle), chime (Packeis), xylophone (Knochenblock), iron\_xylophone (Eisenblock), cow\_bell (Seelensand), didgeridoo (Kürbis), bit (Smaragdblock), banjo (Heuballen), pling (Leuchtstein).

Die Köpfe von Mobs spielen deren Geräusche: zombie, skeleton, creeper, dragon, wither\_skeleton und piglin. custom\_head klickt nur. Seit Minecraft 26.1 gibt es auch trumpet, trumpet\_exposed, trumpet\_weathered und trumpet\_oxidized.

- `note` INTEGER: der Ton, 0 bis 24
- `instrument` STRING: das Instrument (optional, Standard "harp")

```c0de
CALL c0de/minecraft/SOUND
  PARAMETER note 12 ;
  PARAMETER instrument "bell" ;
```
