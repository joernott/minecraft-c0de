```json
{
  "title": "Musik machen",
  "icon": "c0de:include_block",
  "category": "libraries",
  "ordinal": 20
}
```

Ein kurzes Beispiel: einen Glockenton am Auslöserblock spielen.

```c0de
CALL c0de/minecraft/SOUND
  PARAMETER note 12 ;
  PARAMETER instrument "bell" ;
```

`note` geht von 0 bis 24, wie die Klicks auf einem Notenblock. Ohne `instrument` benutzt SOUND die Harfe.
