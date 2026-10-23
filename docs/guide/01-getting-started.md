# Getting Started

c0de is a small programming language inside Minecraft. You write programs by placing blocks, and you run them with trigger blocks. This chapter shows you around and walks you through your first program.

## What is c0de?

c0de is inspired by the old BASIC language and by the early Lego Mindstorms. Instead of typing text, you place colorful language blocks in a row. Each row is one line of your program.

Programs live in their own dimension, the c0de dimension. The trigger blocks that start them can stand anywhere: in your base, in the Nether or right next to the program itself.

All c0de blocks and items are crafted from vanilla materials. You don't need to know how to program. This guide explains everything step by step.

## Entering the dimension

Craft a **trigger block** and a **wrench** (see the chapter on tools). Place the trigger block somewhere. Then right-click it with the wrench.

You are teleported into the c0de dimension. If the trigger block is not set up yet, you land in front of the first free sheet. Otherwise you land in front of the program it runs.

The dimension has no mobs, no weather and no night. The sky is always blue. To leave, right-click the black floor with an empty hand. You return to the place where you entered.

## Sheets

The floor looks like old computer paper with green and white stripes. Each strip of paper is a **sheet**. A sheet is 32 blocks wide and runs south without end.

Every sheet holds at most one program. The black area outside the sheets belongs to nobody. You can place trigger blocks there to start your programs.

## The sheet layout

A program starts at the north end of a sheet and runs south. Each row of blocks across the sheet is a **line**. The first line is line 0.

Inside a sheet, x counts from the west edge. Language blocks go between x=5 and x=28, so a line holds at most 24 blocks. That keeps lines short and easy to read.

At x=4 there is a gray rail: the **cursor rail**. When a program runs, a glowing cursor moves along it and shows which line is working right now.

## Line numbers

At x=2, the floor shows the number of every line that has blocks. Its color tells you how the line is doing:

- faint: a NOOP or COMMENT line
- dark green: the line is correct
- dark yellow: correct so far, but the statement is not finished yet
- dark red: something is wrong

Dark yellow is normal while you build. A BEGIN without its END shows dark yellow until you place the END. The debugging tool tells you what is wrong with a red line.

## Placing blocks

All language blocks are as high as a slab. You place them on the floor between x=5 and x=28. The first block of a line is the **keyword block**. It decides what the line does.

Some mistakes are stopped right away. If a block may not go somewhere, it is not placed and you get a message with the reason.

Some blocks need more information. Right-click a LABEL, LITERAL, OPERATOR, COMMENT or TAG block with an **empty hand** to open its dialog. Click the green check mark to save.

A **LABEL** holds a name, like the name of a function. A **LITERAL** holds a value, like 42 or "Hello". An **OPERATOR** holds a symbol, like + or the `;` that ends a calculation.

## Reading the examples

This guide writes programs as text. Each text line is one line on the sheet. Four spaces mean one block further east (to the right).

Words in capitals like `FUNCTION` are keyword blocks. Lowercase names are LABEL blocks. Numbers and text in quotes are LITERAL blocks. Symbols like `+`, `⊕` and `;` are OPERATOR blocks.

`//` stands for a COMMENT block. The text after it is the comment.

## Your first program

Let's build a function that returns the number 42. Here is the whole program:

```c0de
PROGRAM first
NOOP
NOOP
FUNCTION answer
    RESULT result INTEGER
BEGIN
    ASSIGN result 42 ;
END FUNCTION
```

## Step by step

- Line 0: a PROGRAM block at x=5, then a LABEL named `first`.
- Line 1: a NOOP block at x=5. Every program needs it here.
- Line 2: another NOOP. A FUNCTION always needs a NOOP line right above it.
- Line 3: a FUNCTION block at x=5, then a LABEL named `answer`.

- Line 4: a RESULT block at x=6, a LABEL named `result` and an INTEGER type block.
- Line 5: a BEGIN block at x=5.
- Line 6: an ASSIGN block at x=6, a LABEL `result`, a LITERAL 42 and an OPERATOR `;`.
- Line 7: an END block at x=5, followed by a FUNCTION block.

When all line numbers are dark green or faint, the program is ready. Leave the line after the END empty. The first empty line ends the program.

## Setting up the trigger

Leave the dimension and go back to your trigger block. Right-click it with an empty hand to open its dialog.

Pick your program `first` in the program list. Then pick the function `answer` in the function list.

## Variable items

Programs get and return data as **variable items**. Our function has one result and no parameters, so it needs one empty item to write its result into.

Craft an uninitialized variable item from a **data item** and **light blue dye**. Put it into the input inventory of the trigger block.

## Running it

Give the trigger block a redstone pulse, for example with a button. The program runs on its sheet in the c0de dimension. While it runs, a PID block and a cursor sit on the cursor rail.

When it is done, the output inventory holds a variable item. Take it out and right-click while holding it. The dialog shows the name `result`, the type INTEGER and the value 42.

Congratulations, you wrote your first c0de program!
