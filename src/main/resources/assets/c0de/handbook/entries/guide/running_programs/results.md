```json
{
  "title": "Results",
  "icon": "c0de:trigger_block",
  "category": "running_programs",
  "ordinal": 6
}
```

When a process ends normally, the output inventory gets one variable item per result of the entry function. Each item carries the name, type and value of its result. An EXIT counts as a normal end: the results get the values they have when EXIT runs.

If the function has fewer results than parameters, you also get empty variable items back. So the number of items going in and out is the same.
