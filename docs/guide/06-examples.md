# Examples

Here are a few complete programs to build and try. They come from the test suite of the mod, so they are known to work. Build them on an empty sheet, or change them and see what happens.

## Hello with defaults

```c0de
PROGRAM greeting
NOOP
NOOP
FUNCTION greet
    PARAMETER name STRING "World"
    PARAMETER mark CHAR '!'
    RESULT result STRING
BEGIN
    ASSIGN result "Hello, " ⊕ name ⊕ mark ;
END FUNCTION
```

The function joins three parts into one text with `⊕`. Both parameters have default values, so a caller may leave them out. Called with the name "c0de", the result is "Hello, c0de!".

To try it, set the trigger block to `greet`. Put in a STRING variable item named `name` with your name as its value.

## Adding up numbers

```c0de
PROGRAM adder
NOOP
NOOP
FUNCTION sum
    PARAMETER n INTEGER
    RESULT result INTEGER
BEGIN
    IF n = 0 ;
    BEGIN
        RETURN
    END IF
    CALL sum
        PARAMETER n n - 1 ;
        RESULT result result
    ASSIGN result result + n ;
END FUNCTION
```

This function adds all numbers from 1 to `n`. It calls itself with `n - 1` until `n` is 0, then adds `n` on the way back. For `n` = 10, the result is 55.

To try it, put in an INTEGER variable item named `n`. Each call uses room on the cursor's stack, so very large numbers can crash it.

## Prime numbers

```c0de
PROGRAM primefinder
NOOP
NOOP
FUNCTION main
    RESULT primes STRING
BEGIN
    FOR candidate INTEGER 2 ; 20 ;
    BEGIN
        VARIABLE prime BOOLEAN TRUE
        FOR divisor INTEGER 2 ; candidate - 1 ;
        BEGIN
            IF candidate \ divisor = 0 ;
            BEGIN
                ASSIGN prime FALSE ;
                BREAK
            END IF
        END FOR
        IF prime ;
        BEGIN
            ASSIGN primes primes ⊕ candidate ⊕ " " ;
        END IF
    END FOR
END FUNCTION
```

The outer loop tries every number from 2 to 20. The inner loop checks if a smaller number divides it with no remainder (`\`). If so, it is no prime, and BREAK stops the inner loop. The result is "2 3 5 7 11 13 17 19 ".

## Math library

```c0de
PROGRAM math
NOOP
INCLUDE c0de/math
INCLUDE c0de/strings
NOOP
FUNCTION main
    RESULT rounded INTEGER
    RESULT quotient INTEGER
    RESULT root FLOAT
    RESULT circle STRING
BEGIN
    CALL c0de/math/ROUND
        PARAMETER value 12.5 ;
        RESULT result rounded
    CALL c0de/math/DIV
        PARAMETER dividend -7 ;
        PARAMETER divisor 2 ;
        RESULT result quotient
    CALL c0de/math/SQR
        PARAMETER value 9 ;
        RESULT result root
    CALL c0de/strings/FORMAT
        PARAMETER value c0de/math/PI ;
        PARAMETER format "%.2f" ;
        RESULT result circle
END FUNCTION
```

This program loads two libraries. ROUND turns 12.5 into 12, DIV divides -7 by 2 and gives -3, and SQR gives 3.0. FORMAT shows PI with two decimals: "3.14". The function has four results, so put in four uninitialized variable items.

## Date and time

```c0de
PROGRAM time
NOOP
INCLUDE c0de/strings
INCLUDE c0de/time
NOOP
FUNCTION main
    RESULT now STRING
    RESULT week INTEGER
    RESULT weekday INTEGER
    RESULT later TIME
BEGIN
    VARIABLE t TIME
    CALL c0de/time/NOW
        RESULT result t
    CALL c0de/strings/FORMAT
        PARAMETER value t ;
        PARAMETER format "%tF %tT" ;
        RESULT result now
    CALL c0de/time/WEEK
        RESULT result week
    CALL c0de/time/WEEKDAY
        PARAMETER time t ;
        RESULT result weekday
    ASSIGN later t + 1000 ;
    CALL c0de/time/SLEEP
        PARAMETER ticks 2 ;
END FUNCTION
```

NOW reads the clock, FORMAT turns it into text like "2026-10-19 12:05:09". WEEK and WEEKDAY give the week of the year and the day (1 is Monday). Adding 1000 to a TIME adds one second. SLEEP waits two game ticks before the end.

## A crash on purpose

```c0de
PROGRAM errors
NOOP
NOOP
FUNCTION divide
    PARAMETER a INTEGER
    PARAMETER b INTEGER
    RESULT result INTEGER
BEGIN
    VARIABLE quotient FLOAT
    ASSIGN quotient a / b ;
END FUNCTION
NOOP
FUNCTION main
BEGIN
    CALL divide
        PARAMETER a 1 ;
        PARAMETER b 0 ;
END FUNCTION
```

Dividing by zero raises DIVISION_BY_ZERO in `divide`. The CALL in `main` has no ERROR variable to catch it, so the error moves up. In the entry function, it crashes the process. The trigger dialog shows the error and line 9, and a comparator reads 15.
