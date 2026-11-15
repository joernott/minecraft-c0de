```json
{
  "title": "Zeitgeberblock",
  "icon": "c0de:timer_block",
  "category": "starting_programs",
  "ordinal": 1
}
```

Der Zeitgeberblock startet einen Prozess immer wieder, in einem Intervall, das du einstellst.

Sein Dialog funktioniert wie der eines Auslöserblocks, mit einem zusätzlichen Feld: dem Intervall in Ticks. 20 Ticks sind eine Sekunde. Ein neuer Prozess startet erst, wenn der letzte beendet ist.

Ein Redstone-Signal pausiert den Zeitgeberblock. Mit einem Hebel schaltest du ihn aus und ein. Ein Blatt kann nicht geändert werden, während ein Prozess seines Programms läuft. Pausiere also einen schnellen Zeitgeber, bevor du sein Programm änderst\; der Dialog zeigt dann „Durch Redstone pausiert“. Ein Zeitgeberblock, der neben ein Signal gesetzt wird, das schon an ist, startet pausiert. Wie der Auslöserblock kann er mit einem beliebigen Block im Tarnungsplatz getarnt werden.

<recipe;c0de:timer_block>

Kupferbarren in den Ecken, Glas an den Kanten und eine Uhr in der Mitte ergeben acht Zeitgeberblöcke.

Stelle einen Zeitgeberblock auf die Funktion `ring` und ein Intervall von 20 Ticks ein. Er spielt jede Sekunde einen Glockenton. Setze einen Hebel daneben, um das Läuten zu stoppen.

```c0de
PROGRAM chime
NOOP
INCLUDE c0de/minecraft
NOOP
FUNCTION ring
BEGIN
  CALL c0de/minecraft/SOUND
    PARAMETER note 12 ;
    PARAMETER instrument "bell" ;
END FUNCTION
```
