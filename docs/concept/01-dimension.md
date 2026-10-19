# The c0de dimension

[c0de concept](../concept.md) › The c0de dimension

## Properties

The c0de dimension has the following properties:
  - no mob spawning
  - no sun and no daylight cycle
  - no weather
  - a cloudless blue sky
  - a constant ambient light level of 12
  - a floor at y=-4 made of the following unbreakable and uncraftable blocks:
    - white c0de bedrock (texture similar to white concrete, with an off-white color #F5F7F3)
    - light green c0de bedrock (texture similar to concrete, with a very bright green color #D2EBD7)
    - black c0de bedrock (texture similar to black concrete, color #000000)
    - cursor c0de bedrock (texture similar to gray concrete with a single rail in its middle, always running in z direction)
  - the code layer at y=-3, directly on top of the floor
  - a configurable maximum build height between y=15 and y=2031 (default y=255). Minecraft requires the bottom and the height of a dimension to be multiples of 16, so the dimension starts at y=-16 and the maximum build height MUST be a value of 16·k−1. The maximum build height limits the number of processes that can run for one program at the same time (PIDs 0 up to the maximum build height, 256 by default).

## Terrain generation

The floor of the dimension resembles 32 block (2 chunk) wide greenbar paper ("Tabellierpapier") running in north-south direction, starting at x=0, z=0.

All chunks with a negative x or a negative z coordinate have a floor consisting only of black c0de bedrock.

Chunks with positive x and z coordinates form a 2 chunk wide pattern:

![floor_c0de](../images/floor_c0de.svg)

This creates endless sheets of greenbar paper next to each other. Each sheet is 2 chunks wide in x direction and endless (up to the maximum z coordinate) in z direction. The relative x coordinate 4 of every sheet is made of cursor c0de bedrock and forms the cursor rail. Code is placed between x=5 and x=28.
