# Prompts

This file collects all prompts used to work on this repository.

## 2026-10-18 – Concept review

In the minecraft-c0de repository, I want to create a minecraft mod that implements a primitive programming language. The folder docs contains the concept as far as I have written it.

Please review the document, fix all typos and grammatical errors.

Then, analyze the concept and look for possible mistakes, refine and structure it and add missing descriptions for the remaining statements a cursor can encounter.

Always save all prompts in a file zz_prompts.md in the docs folder.

## 2026-10-18 – Answers to the review questions

Answering the questions from the review:

1. A single block whose symbol is chosen in a dialog
2. Define the functions you outlined. Complement them by functions can be taken from AMIGA Basic (see https://archive.org/details/amiga-basic-handbuch/page/n3/mode/2up and following pages), except for functions that are file system or graphics related.
3. Yes.
4. Triggers are allowed in the c0de dimension in the negative area
5. While processes are running, editing must be forbidden

## 2026-10-18 – Answers to the remaining review questions

6. Yes
7. Leave the threading out for now
8. Literal items are not needed at the moment, remove them
9. Function items are put on the stack by the cursor, whenever it enters a function. When leaving a function, result values are copied to the variables of the calling function and everything on the stack up to the function item of the function is removed.
10: Yaml import/export should be handled by a dedicated tool
11. The label should not display the index. Block properties should be shown in a dialog, when clicked with the debug tool.
12  That is intentional to encourage smaller functions and simpler expressions
13. The stack size should be the limit
14. That is the intended way.

There will be an optional c0de/minecraft library that has to be included. Move all Minecraft related functions there.

The exit code 15 can be set by the program to indicate a fatal runtime error if the author decides it.

## 2026-10-18 – Answers to the open questions (YAML, SLEEP, c0de/minecraft)

Here my answers:
1: The yaml file should have a key "PROGRAM" with its name as value. The key "INCLUDE" contains an array of all includes. The key "code" should be an array of nested objects that contains every other statement. Each object has a keyword and name property, it may have other properties like type, value etc. If it contains a code block, the content of that should be parsed into anested array defined by the code key.
2. That is fine
3. The library should contain functions to interact with buttons, levers, pressure plates, doors, trapdoors and fence gates. It should also be able to get block data from a specified coordinates and write chat messages. The ability to change inventory content, place and remove blocks, spawn and teleport items or entities should be implemented but require a setting in the mods configuration file to be used by someone without admin privileges.

## 2026-10-18 – FIND_ENTITIES and SAY radius

1. That should be unrestricted
2: The radius should be configurable

## 2026-10-19 – COMMENT, TAG, libraries, JSON/YAML and splitting the concept

I have installed python on this machine, so you can use it if it makes your work easier.
I want to implement the following changes:

I want to introduce a new keyword COMMENT. A comment should be placeable at the end of a line. It has a property "text" that a user can edit by right clicking the comment and editing it in a dialog. When a cursor encounters a comment, it MUST stop parsing the line, allowing a user to "comment out" code. Eight comment blocks can be crafted from a glass block surrounded by NOOP locks.

I also want to introduce Golang style tags that can be put behnd struct field by introducing a TAG keyword. A tag has two properties:
- The language, e.g. json or yaml
- A name that substitutes the key name.
A Tag can also be added to a variable declaration to allow extracting a single value from a json or yaml structure into that variable. Eight tags can be crafted by urrounding a brown wool with eight NOOP blocks.

The third change, I want to introduce is moving all mathematical functions to a library c0de/math, all string functions except LEN to the library c0de/strings and all time/date relted functions to c0de/time

There should be four new functions in the c0de/strings library: to_json, to_yaml, from_json, from_yaml that convert variable content to a json or yaml string and vice versa. These should make use of tags. If the annotatio of a simple type or string variable has a name a.b, a mathcing structure is created when using to_yaml or to_json. When using from_yaml or from_json, only the content of exactly that field is extracted into the variable.

As the document has gotten very long, please split it into multiple documents, focusing on one topic each.

## 2026-10-19 – NOOP lines, array paths in tags, FROM_JSON/FROM_YAML

Answering your questions:
1. NOOP lines should be empty
2. Yes

FROM_YAML and FROM_JSON should have two parameters, the source which must be a string and the destination which is a previouly created variable. That variable already has a type. If the variable has annotations, those shall be used to translate the names. If the function encounters elements in the json or yaml that do not exist in the target, those are ignored. If types don't match, a TYPE_MISMATCH error is raised. I don't want to introduce something like map{string]interface{} or a data type VARIANT.

## 2026-10-19 – TIME data type, time functions, PI and FORMAT

Please add a new data type TIME that contains a unix timestamp.

Add a function NOW to the time library that delivers the UNIX timestamp into a variable of that type.. Also add the function SECOND that delivers the seconds passed in the current minute as integer, MINUTE that delivers the current MINUTE in the hour, HOUR that delivers the current hour in a day (24h format), as well as DAY, MONTH and YEAR. The function WEEKDAY delivers a number between 1 and 7 where 1 is Monday and the function WEEK delivers the week of the year, both following ISO-8601-Standard

Add PI as a constant of type FLOAT to the math library
Also add a string function FORMAT, that has two parameters, a Variable to be converted and a format string based on the printf format string. This replaces DATE, TIME

## 2026-10-19 – Time zone and TIME precision

1. The functions should use the servers time zone
2. Use milliseconds

## 2026-10-19 – Implementation plan and development environment

Please design an implementation plan grouped into sprints and tasks.

Also write up a plan for setting up a development environment. The goal is to develop a mod that supports both neoforge and fabric. Initial development should happen in Minecraft 1.21.1 and later support all newer releases.

I have pushed the repository to github, so we can use github actions.

There is a development VM I have been using for the doenerstag application. It has docker installed and should have enough resoures to install whatever might be needed for testing. You can log in as user doener with your ssh key and use passwordless sudo.

## 2026-10-19 – Answers to the open questions of the implementation plan

1. A player can enter the dimension by right clicking a trigger block with a wrench. He can exit the dimension by right clicking a black floor block in the c0de dimension.
2. Make that de.joernott.c0de
3. Let's stick to BSD 3 clause
4. Initially Modrinth. I already have an account there, linked to the github account and use the modrinth loader.

The disk size of the VM has been increased

## 2026-10-19 – Marking completed tasks

please mark completed tasks in the implementation plan with a checkmark. If you discover that an additional task is needed to complete one task in a sprint, add it as a next level number, e.g. an additional task for 0.1 becomes 0.1.1

## 2026-10-19 – Start sprint 1

please create a sprint-1 brnch and start implementing sprint 1

## 2026-10-19 – Merge sprint 1

please fast forward main to sprint 1 and push it.
I have run gl auth login

## 2026-10-19 – Start sprint 2

please continue with sprint 2

## 2026-10-20 – Merge sprint 2, start sprint 3

The light level is fine, the sky is empty and blue and the floor texture looks right. Please merge sprint-2 into main and start with sprint 3

## 2026-10-20 – Merge sprint 3, start sprint 4

yes, blease fast forward main to sprint-3 and start sprint 4

## 2026-10-20 – Merge sprint 4, start sprint 5

Please go ahead as suggested

## 2026-10-20 – Merge sprint 5, start sprint 6

yes, please continue

please retry

## 2026-10-20 – In-game check of sprint 6

I have run the above tests: Here are my findings:
1: Yes, they're fine
2: Yes, they appear correctly
3: The comment appears above the block but depending on the cursor position on the block, parts of it disappear, as if there is something in front of them, that is blocking the sight but is invisible at the same time. I have taken 2 Screenshots of it: 2026-10-20_21.26.57.png and 2026-10-20_21.27.01.png
4. Yes, the are
5. Yes, they do. BUT on the operator block I placed, the + sign on top seems to be dark, while the ones on the sides are bright. I have  taken the Screenshot 2026-10-21_21.30.19.png to show that

## 2026-10-20 – Finish sprint 6, start sprint 7

Both are fine now, go ahead, finish sprint 6 and continue with sprint 7

## 2026-10-20 – In-game check of sprint 7

Findings: A program existing from a previous run has not been considered, when creating another one in the second sheet with the same name. However, creating a program with the same name in the third sheet failed as expected.

Placing a parameter block in the same line as the function was blocked. However, I could place it a x=2, x=3

I could place an ASSIGN block on the same level as the begin above, it should be indented. I could also place it more to the left

The name of a result did not appear in the list of names when placing a label next to assign. However some error constants were available. It should not be possible to assign a value to those

Also, parameters to a function should be available when placing labels.

The function looks like this:
FUNCTION T7
 PARAMETER In STRING LITERAL "Hello World"
 RESULT Out
BEGIN
 ASSIGN Out In OPERATOR ⊕ LITERAL "!" CHAR
END

When placing the END block it did not brighten. I forgot to place a STRING as last element for the Input
What other mistake did I make?

## 2026-10-20 – Function T7 still invalid

Sorry, I placed the STRING after Out, I just forgot to type it
I've added the Type after the LITERAL for PARAMETER In. RESULT Out already had a type, I just forgot to type it. I also added the Semicolon to the ASSIGN  line and the FUNCTION to the END line. The function did not light up though

## 2026-10-20 – Findings after fixing the programs

The program on sheet 2 lit up now. On sheet1, I fixed the program, too and it lit up. I thested the new placement restrictions and they look OK.

Other Findings: I accidentally broke a floor block. Middle clicking it, did not allow me to get it to repair the floor. In creative mode, this should be possible (like with bedrock), as it is possible to break the floor. Also, the triangle on the assign block should always point to positive x on all sides.

## 2026-10-20 – Finish sprint 7, start sprint 8

The texture is right and I was able to fix the hole in the ground. You can mark the sprint as done and proceed as usual

## 2026-10-21 – In-game check of sprint 8

1:Yes
2: cursor appears and moves down the sheet. A PID block appears and has the PID on its north and south side and the references on the east and west side.
Findings:
- Clicking on the green checkmark of the Trigger Dialog did not close the Dialog.
3: I get the message
4: Yes
5: Yes

Instead of a spinning arrow, the cursor should be a cuboid that is slowly pulsing between dark green and lime. A debug cursor should pulse between dark red and red

Please note, I have installed the mods Jade and EMI to help me crafting and identifying blocks. Please tell me if that interferes with anything.

## 2026-10-21 – Cursor shape

The dialog now closes properly. The cursor pulses correctly but looks like a half slab. It should be an upright cuboid, the height about 100% of a block, the width about 75% of the block and it should be centered in the middle of the block.

## 2026-10-21 – Cursor spawn position

Please let it spawn at z=1, that is one of the reasons, there is a NOOP at z=1, as this means, the cursor only needs to process something when entering a new line. Not parsing a NOOP does not cause a problem.

## 2026-10-21 – Finish sprint 8, start sprint 9

The look of the cursor is fine now, sou you can finish Sprint 8 and continue with Sprint 9

## 2026-10-21 – In-game check of sprint 9

1:
- Yes, labels are readable, the dialog for the PID block is readable
- Left clicking the trigger block with the debug tool spawned the cursor and it is pulsing red
- Left clicking the cursor and shift-left clicking it with the debug tool worked
- Right clicking the cursor worked as well, I can see the stack
2. Marking lines work and the overlay is visible and runs from x=5 to x=28
3. Moving lines worked but I could not unselect the marked lines and I could not mark new lines

## 2026-10-21 – Marks after moving lines

I still can't unselect the selected lines. I also can't select new lines

## 2026-10-21 – Floor clicks and a more intuitive wrench

1: Yes
2: Works
3: Works
4: Yes

It should be possible to click on the floor of a line as well, not only on a piece of code in that line.That applies to both marking and selecting items.

We should also introduce one change that mkes the wrench more intuitive. We should eliminate the need to click a marked area to select it but rather consider the marked lines as selected. Then, left clicking the target line with the wrench would move the selection there, shifting tht line and all lines after it south. Right clicking a target line would copy the code in the selection there, shifting that line and all lines after it south

The workflow got stuck:
 [20:06:10] [Server thread/INFO] (Minecraft) test-mock-player left the game
[20:06:10] [Server thread/INFO] (Minecraft) Saving worlds

It has the same issue again

## 2026-10-21 – Text under marks

All four tests work as expected. However, I've noticed that the text on labels and literals disappears when a line is marked. It reappears, when the line is unmarked.

## 2026-10-21 – Text under marks, second attempt

I marked a line and it looks differently now, even better than before.But the tet on labels and literals still vanishes when a line is marked.

## 2026-10-21 – Finish sprint 9, start sprint 10

The text is now readable on marked lines. Please proceed with sprint 10

## 2026-10-22 – Uninitialized variables

Tests 1 to 4 worked, I did not test #5 yet. Because I encountered  bug.

If there are more results than parameters to a function, the function wants an uninitialized variable in it's inputs. Currently we don't have those, as they already have a type. It is not possible to remove the type from a variable item, because you can't click the button if it is missing.

Please add an item "uninitialized variable" that can be crafted just with the data item and dye. Add a trashcan button in the dialog that erases all property content. Also allow the player to clean the three fields by themselves. Both actions result in an unitilaized variable item.

## 2026-10-22 – Permissions, uninitialized inputs and output space

Tests 1-3 are fine
Test 4 failed: I placed a block and used REMOVE_BLOCK to break it. Then, I replaced the block, changed my game mode to survival before clicking the debug trigger with the debug tool. Then I shift clicked the cursor again and it broke the block again.

I also noted, that all items from the input are transferred to the output of the trigger block. Only initialized variables should be moved and the number of uninitialized variables reduced by the needed amount. The rest of them should remain. Thus, one can stack them in the input.

Another thing: Triggering a run should always work, if there is enough space in the output inventory. Only if it is full, a trigger should fail.

## 2026-10-22 – Pass initialized items through

Before I start, please also move all initialized variables from the input to the output, even those that are not needed for the current program. This way, one can chain multiple triggers and feed the output of one into the second, where only some of it is needed and at the end of the chain, I can collect everything from all programs.

## 2026-10-22 – Chained triggers and LAN permissions

1: works as expected,
3: worked as well
2: I placed a second trigger behind my debug trigger. The output of the debug trigger was not fed into the only adjacent inventory (the next trigger). I repeated the test with two regular triggers but that didn't work either.
4: I opened to lan, set the game mode to survival and cheats off. Then I placed a new trigger and configured it and that one could still break the block. 
I changed game mode to survival in the chat, just to make sure and repeated the test and it broke the block again

## 2026-10-22 – Hoppers and pipes for trigger blocks

let's go with solution 1. Most of my modded worlds also include pipes and cables, so that should work in a more compact way.

## 2026-10-22 – Sprint 10 checked

All three tests worked fine now

## 2026-10-22 – Finish sprint 10, start sprint 11

Fast forward main but change the version to be released to 0.1.0 as this is the first alpha of the mod. 

After 11.1, there should be another task: Different shapes of blocks. The PARAMETER block should actually take its model from an actual hopper buit it should be recolored. The RESULT block should use the same model but inverted. the begin and end shapes should become actual trapezoids.

Also add another task below that: When a line has any content, show an overlay with the line number on the floor of the block at x=2. The line number should have a faint difference to the underlying blocks color for NOOPS and COMMENTS. For actual statements, it should be dark red if that line is incomplete or syntactically wrong, dark yellow if it is an incomlete statement like begin without an end, a function missing the code block etc. It should be dark green if that single line is syntactically correct (or in case of a multiline statement, the whole statement is correct)

Complete 11.1 and these new 11.2 and 11.3 tasks, then let me have a look.

## 2026-10-23 – Trapezoids with diagonals

The tests were fine and provided the expected feedback. The trapezes for begin and end should be laying on their sides and ther should be actual diagonals not steps. The Create: copycats+ module provides slopes that are actually triangular and Create: Extra Copycats provides even more variants of shapes with diagonal lines. The latter has been designed with AI support. Please have a look at them to see if we can use that technique to create a trapezoid lying on its trapez side.

## 2026-10-23 – Slab-high blocks, unlit colors and new shapes

yes to all three.

I have a few more changes I want to mimplement:
- Blocks should only have the height of a half slab, There should not be any symbols on the sides. Resize the PARAMETER and RESULT blocks zo 50% to fit the height of a slab but retain the proportions
-The colors are too bright, Just use the unlit color for them and remove the lighting logic, as we have the line numbers now indicating if a line is correct.
- The corners of a NOOP block should be as wide as the sides
- The left side of PROGRAM should be a semi circle, while the right side remains a cuboid. Remove the Symbol on it.
- Instead of operators being blocks, I would like to have them be just the letter/symbol on the floor extruded into the 3rd dimension.
- For variables use a chest as model but retexture it blue. Constants should use a barrel as model, They should also be scaled down to fit the slab height.
- Literals should be turned into octagons to distinguish them better from labels.
- The circular arrow on the for loop needs to be thinner and the # sign sharper and they should not bleed into each other. Also, as we now use dark green for the block color, we can use lime for them.

## 2026-10-23 – Hammer and marker right click, feather marker

The blocks look fine so far.

I would like to add a new functionality to the hammer: Right clicking a line or a block on a line should move that line and every line after to the south

The marker should get a right click functionality. It should remeber the last line that it was used on and whether this was marking or unmarking the line. If you then right click another line, all the lines between the last one and the line that was right clicked become either marked or unmarked, depending on what happened on the line that was left clicked.

The model of the marker should be that of a Minecraft feather but colored yellow.

## 2026-10-23 – Item-frame symbols, new shapes, dialogs and aborting processes

Marker and hammer work perfectly.

I have the following changes I'd like you to implement:
- The program block should get the same texture as the function block. Put the image of a Minecraft book on it like it would look if it was placed in an invisible item frame.
- In the inventory, the operator block looks like a +, but turns into a question mark, when placed. It should also look like a question mark in the inventory to avoid confusion.
- The ASSIGN operator should become a triangle lying on its side and pointing to positive X
- Remove the need for indenting INCLUDE statements. Variables, constants and functions are also placed on X=5. The symbol on the Include statement should look like a Minecraft bookshelf if it was placed in an invisible item frame.
- The COMMENT should no longer be a block, just two green slashes that are almost fitting the x and z dimensions, extruded from the letters on the floor, similar to the operators. 
- BEGIN and END in the inventory should be swapped
- IF and ELSE should become dark green raute blocks lying on their sides with the question mark in lime on their top.
- Having only buttons in the selection list for labels shows too few options. Please turn it into a scrollable list that is filtered by what is entered in the text box
- When initializing variables, the TYPE field should become a dropdown with all types available
- Dialog corners should be slightly rounded
- Shift-Right clicking a PID block with the debugging tool should abort a program.

## 2026-10-23 – Dropdown hint, item orientation, PROGRAM border, WHILE loop bug

All tests ran fine, Please put a small triangle at the end of the dropdown for the type selection to hint at it being a dropdown.
Findings:
- The border of the texture of the Program block does not fllow the curve but seems to be still a square.
- The operator item looks like an upside down question mark and should be turned 180 degrees
- The assign item as well
- In the world "Testing", the function Test8 in the program "Sprint8Test2" contains a while loop: WHILE j <= 5 ;
Outside the loop, the variable j is initialized as 0.
Inside the loop, I have ASSIGN j j + 1; which I intended to increase j until it hits 5. That does not happen and I had to abort the program. Please investigate, why.
