```json
{
  "title": "Including libraries",
  "icon": "c0de:include_block",
  "category": "libraries",
  "ordinal": 2
}
```

INCLUDE lines come right after the NOOP in line 1, before any other statement. They start at x=5, like the PROGRAM line.

```c0de
PROGRAM math
NOOP
INCLUDE c0de/math
INCLUDE c0de/strings
```

The LABEL dialog next to an INCLUDE block lists all libraries you can choose from.
