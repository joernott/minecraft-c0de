# c0de

## Abstract

c0de is a simple interpreted programming language implemented as a Minecraft mod. The mod adds its own dimension in which players place blocks to assemble a program. All of these blocks can be crafted from vanilla materials. Programs are run by triggering special blocks that can be placed in any dimension.

## Introduction

c0de is inspired by the programming language BASIC and by the early Lego Mindstorms, where users programmed the "Brick" in an IDE by connecting colorful blocks that looked like Lego bricks. In the same spirit, a c0de program is written by placing c0de blocks in a special Minecraft dimension.

Programs can be imported and exported as a textual representation in a YAML file with the [c0de disk](concept/14-items-and-tools.md#c0de-disk). The file can be edited in an IDE, but where is the fun in that?

### Requirement keywords

The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD", "SHOULD NOT", "RECOMMENDED", "MAY", and "OPTIONAL" in this document are to be interpreted as described in RFC 2119 and summarized below.

  - MUST: This word, or the terms "REQUIRED" or "SHALL", means that the definition is an absolute requirement of the specification.
  - MUST NOT: This phrase, or the phrase "SHALL NOT", means that the definition is an absolute prohibition of the specification.
  - SHOULD: This word, or the adjective "RECOMMENDED", means that there may be valid reasons in particular circumstances to ignore a particular item, but the full implications must be understood and carefully weighed before choosing a different course.
  - SHOULD NOT: This phrase, or the phrase "NOT RECOMMENDED", means that there may be valid reasons in particular circumstances when the particular behavior is acceptable or even useful, but the full implications should be understood and the case carefully weighed before implementing any behavior described with this label.
  - MAY: This word, or the adjective "OPTIONAL", means that an item is truly optional. One vendor may choose to include the item because a particular marketplace requires it or because the vendor feels that it enhances the product, while another vendor may omit the same item. An implementation which does not include a particular option MUST be prepared to interoperate with another implementation which does include the option, though perhaps with reduced functionality. In the same vein, an implementation which does include a particular option MUST be prepared to interoperate with another implementation which does not include the option (except, of course, for the feature the option provides).

### Glossary

After this nod to the famous terminology definition used in many RFCs, we return to our regular program:

  - **sheet**: An area of the c0de dimension that is two chunks (32 blocks) wide in x direction and extends from z=0 up to the maximum z coordinate. Sheet n covers the absolute x coordinates 32·n to 32·n+31 (n ≥ 0). A sheet holds at most one program.
  - **relative x coordinate**: The x coordinate relative to the western border of a sheet (absolute x modulo 32). Unless stated otherwise, x coordinates in this document are relative, y and z coordinates are absolute.
  - **code layer**: The layer y=-3, directly above the floor. All blocks of a program are placed on this layer.
  - **line**: All blocks of a sheet on the code layer that share the same z coordinate and lie between x=5 and x=28. A line is identified by its z coordinate. A line holds at most 24 blocks. This limit is intentional: together with the indentation, it encourages small functions and simple expressions.
  - **empty line**: A line that contains no blocks at all (only air). A line containing a NOOP block is *not* empty, even though it looks empty because the NOOP block is transparent.
  - **keyword**: A language element such as PROGRAM, VARIABLE or IF that is represented by its own block. Keywords are written in capital letters in this document.
  - **keyword block**: The first (westernmost) block of a line. It determines which statement the line belongs to.
  - **indentation**: The x coordinate of the keyword block of a line. A line is *indented by one block* relative to another line if the x coordinate of its keyword block is exactly one greater.
  - **statement**: One or more lines that together form a language construct as described in this document. A statement is valid if all of its lines comply with the rules for that statement.
  - **continuation line**: A line that belongs to the statement started in a line before it, e.g. the second line of an expression or the PARAMETER lines of a CALL statement.
  - **program**: The statements on one sheet. A program MUST start at z=0 with a PROGRAM statement that MUST be directly followed by a line containing a NOOP statement. A program ends at the first empty line. A program always runs south (from lower to higher z coordinates). See [Programs](concept/03-programs.md).
  - **process**: A running instance of a program. Processes are started by trigger blocks (see [Running programs](concept/13-running-programs.md)). A trigger block MUST NOT start another process while a process it started is still running. Several processes of the same program MAY run at the same time. They are independent of each other: every process has its own cursor and stack, so processes don't share any variables.
  - **process ID (PID)**: An integer that equals the y coordinate of the PID block and of the cursor of a process. Valid PIDs range from 0 to the maximum build height of the c0de dimension. Every program has its own sheet, so a PID is only unique within its program. A process is globally identified by the combination of program name and PID.
  - **cursor**: An entity that moves along the cursor rail (x=4) of a sheet and marks the line that is currently being processed. See [The cursor](concept/15-cursor.md).
  - **before / directly before**: A line with a lower z coordinate in the same program / the line at z-1.
  - **after / directly after**: A line with a higher z coordinate in the same program / the line at z+1.
  - **followed by / directly followed by**: A block in the same line with a higher x coordinate / the block at x+1 in the same line.
  - **code block**: A sequence of lines that starts with a BEGIN line and ends with the matching END line. See [Code blocks and scopes](concept/03-programs.md#code-blocks-and-scopes).
  - **scope**: The region of a program in which a name is visible. The program itself forms the global scope, every code block forms a nested scope.
  - **unique**: A name is unique if it has not been defined in the current scope or in any of its parent scopes and is neither a keyword nor the name of a built-in type, constant or function.

## Documents

The concept is split into the following documents:

**The world**

  - [The c0de dimension](concept/01-dimension.md)

**The language**

  - [Language basics](concept/02-language-basics.md)
  - [Programs and code blocks](concept/03-programs.md)
  - [Data types, variables and tags](concept/04-data.md)
  - [Expressions and assignments](concept/05-expressions.md)
  - [Functions](concept/06-functions.md)
  - [Structural statements](concept/07-structural-statements.md)

**Functions and libraries**

  - [Built-in functions and libraries](concept/08-builtin-functions.md)
  - [The c0de/math library](concept/09-lib-math.md)
  - [The c0de/strings library](concept/10-lib-strings.md)
  - [The c0de/time library](concept/11-lib-time.md)
  - [The c0de/minecraft library](concept/12-lib-minecraft.md)

**Running programs**

  - [Running programs](concept/13-running-programs.md)
  - [Items and tools](concept/14-items-and-tools.md)
  - [The cursor](concept/15-cursor.md)
  - [Error codes](concept/16-error-codes.md)

**Appendices**

  - [YAML format](concept/17-yaml-format.md)
  - [Block reference](concept/18-block-reference.md)
