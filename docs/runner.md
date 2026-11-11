# The program runner

The program runner runs c0de programs without Minecraft. It reads a program in the text notation of the guide ("Reading the examples"), validates it, runs one of its functions and prints what happened. The website c0de.at uses it to test the programs of its tutorials against every release of c0de, so a change of the language that breaks a tutorial is found before the tutorial is out of date.

Every release attaches it to the GitHub release as `c0de-runner-<version>.jar`. It needs Java 21 or newer. Build it yourself with `./gradlew :core:runnerJar` (in `core/build/runner/`).

## Usage

```
java -jar c0de-runner-<version>.jar [--options options.yaml] [--expected expected.yaml] program.c0de
```

| Exit code | Meaning |
|---|---|
| 0 | The program is valid and ran to its end (or was only validated), and it matches the expected values |
| 1 | The program is invalid, didn't end, crashed (without `--expected`) or doesn't match the expected values |
| 2 | Wrong command line, or a file can't be read |

`--version` prints the version of the runner, which is also the value of `C0DE_VERSION` in the programs it runs.

## The program

The file contains the program as the guide writes it: one line of text per line of the sheet, four spaces per block of indentation, keywords in capitals, names, literals and operators in between.

```c0de
PROGRAM doubler
NOOP
NOOP
FUNCTION twice
    PARAMETER n INTEGER
    RESULT result INTEGER
BEGIN
    ASSIGN result n * 2 ;
END FUNCTION
```

The library c0de/minecraft is part of the mod. The runner validates programs that include it, apart from that library, but doesn't run them.

## Options

The options file is YAML. Every key is optional.

```yaml
function: twice               # the entry function (main)
parameters:                   # the values of its parameters, as FROM_YAML reads them
  n: 10
input: [Steve, 21]            # the values for INPUT, in the order the program asks for them
keys: "yn"                    # the keys for INKEY, one character each
seed: 0                       # the seed of RANDOM (0)
time: 2026-01-01T12:00:00Z    # the clock, which stands still (2026-01-01T12:00:00Z)
zone: UTC                     # the time zone (UTC)
columns: 48                   # the size of the screen for PRINT (48 x 20, like a laptop)
rows: 20
max_steps: 1000000            # the program is stopped after this many steps (1000000)
```

Parameters without a value in the file need a default value in the program. A program that waits for more INPUT than the file has is stopped.

## The report

The runner prints a report as YAML:

```yaml
program: doubler
function: twice
valid: true
problems: []                  # line, code and message of every problem
run: true                     # false if the program was only validated
state: FINISHED               # FINISHED, CRASHED or STOPPED
steps: 3
exit_code: 0
results:
  result: 20
screen: []                    # the rows of the screen, without empty rows at the end
written: []                   # the values of WRITE, as name: value
```

A crashed program has an `error` with `code`, `name` (for the errors of the language), `message`, `context` and `line`; a stopped one says why in `stopped`.

## Expected values

With `--expected`, the runner compares the report with a YAML file that contains the parts of the report that matter. Every key of the file must be in the report with the same value; maps may have more keys in the report, lists must be equal.

```yaml
results:
  result: 20
```

A program that crashes on purpose can expect its error:

```yaml
state: CRASHED
error:
  name: DIVISION_BY_ZERO
  line: 9
```

## Libraries of other mods

A mod can bring its libraries and built-in constants to the runner with a jar of its own that implements `de.joernott.c0de.core.library.LibraryProvider` and names it in `META-INF/services/de.joernott.c0de.core.library.LibraryProvider`. Put it on the class path next to the runner:

```
java -cp c0de-runner-<version>.jar:c0de-net-runner-<version>.jar de.joernott.c0de.core.runner.Main program.c0de
```

(on Windows, separate the jars with `;`).
