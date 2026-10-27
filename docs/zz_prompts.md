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

## 2026-10-23 – Wider shaped borders, cylindrical loop blocks

1: Yes
2: Some of the borders have clipped blocks with the inner color "outside" the borders. Maybe make the borders of the round and diagonal parts 2 blocks wide to catch those outside blocks.
3: Yes

Please turn the loop blocks into cylinders.

## 2026-10-23 – CALL symbol, player guide and Patchouli book

1: No, the borders are good
2: Yes, they're perfect

Please replace the phone symbol in the call function with 📞 but just in black.

Also implement 11.4. For that, add another subtask to support documentation in the patchouli format (https://vazkiimods.github.io/Patchouli/). If the mod is installed, the book should be craftable from a book and a noop block. Also, new players should be gifted the book on first join.

## 2026-10-24 – CALL symbol position, guide book recipe

1: The call symbol needs to shift down a pixel or two and the ear piece looks like it is missing a bit.
2: yes
3: The pages are fine
4: I suggested the wrong recipe. Tis is the same recipe as for the PROGRAM block. So, instead of using a NOOP block, let's use a program block and a book to craft the patchouli book.

## 2026-10-24 – Raised symbols, reference handbooks, hammer on empty lines, cursor line in chat

The recipe looks fine. I have redreawn src/resources/assets/c0de/textures/block/call_block.png to look the way I want.

I like the way, the symbol sticks out a pixel from Program and Include. Please do that for all th other symbols on blocks as well. Also design the symbols on the type blocks to actually use a similar technique, with the item, they were crafted from

Please recolor the Patchouli book to be red instead of green. 

In addition to the guide, I'd like more separate red books: For types, literals, constants and variables, one for functions and the other structural blocks, one for tools, one for triggers. Another book should be a function reference. Those should be designed more like a handbook, each with one page per element, Those pages should be structured in the same way:
- The first paragraph is always with a short description at the beginning,
- The second is their crafting recipe
- The third is an example on how to use it

The MD documentation also should have an image for every crafting recipe and lookup pages following the same organisational structure and an index to tie them together.

I want to use the hammer tool on completely empty lines, too.

When left clicking the cursor with the debug tool, it should say the instruction line in the chat to the person who clicked, highlighting in red, when elements are missing or wrong.

## 2026-10-24 – Concept and code, library docs, PRINT/WRITE/INPUT, display, I/O block, laptop and tablet

Please bring the concept in line with the code.
I ran the gabe and tested the 4 things:
1: Yes
2: Yes, they do. Please add the documentation for the new books
3. yes
4. yes

Please remove the lines from the "Variable" items texture. 
Please also add a documentation on how to write c0de libraries in other mods so that they register with c0de

I also want to add some core functions and three more blocks that can be placed everywhere and another Item:
- The main functions are PRINT, WRITE and INPUT. Print and wrrite need an expression after its keyword and Input needs the name of a declared variable. They will be used to interacct with the following blocks and items. Please also add the screen related functions from AmigaBASIC we did not implement due to the lack of a screen. 
- The first block is a display block that can be assembled with other display blocks into a flat horizontal or vertical multiblock structure. It has one borderless front side that is a very dark gray. The remaining sides usually have the texture of an iron block but can be camouflaged. The block can be rotated around the axis going through a face by left clicking that face with a wrench. If another block is placed next to an existing block, it will try to choose the same direction. The blocks connect seamlessly and can be used by the game to output data. Each block can be connected to a program/function in a similar way to a trigger but all connected blocs automatically share that data. Please use the font https://github.com/IdreesInc/Monocraft or use it as as a reference to ensure that text written on the blocks are in a monospace font. Its content will be influenced by the PRINT and other screen related elements. 8 items should be crafted from a central glowstone dust with a redstone dust on its left. the remaining 7 spots are filled with iron nuggets. 

- The next block is an I/O block that looks like a retextured hopper. It is linked to a program/function in the same way, a trigger is. It accepts inputs in the form of Variable/Constant/Literal items. When an INPUT statement is encountered and a variable with the correct/name/type is in any of the connected I/O blocks input inventory, that item will be read. Otherwise, the input function will wait for an item placed into any I/O blocks or the user to enter an input on a laptop or tablet. If multiple I/O blocks have a valid input, the I/O blocks that has been connected earlier will take precedence.  The matching item in the input inventory will be consumed as its value is passed to the program and an uninitialized variable will be placed inthe blocks output inventory, if the input was a variable. If the input is a constant, it will remain unchanged when appearing in the output inventory. As literals don't have a name, the first literal matching the required type will be taken from the input inventory and an empty literal item will be placed in the output inventory. The WRITE function should take an uninitialized variable item from the input inventory of each connected I/O block and put a variable item containing the WRITE functions output into the blocks output inventory. If none of the I/O blocks have uninitialized variables in their input, the function waits for the player to provide it. The shapeless recipe for the bock should be a data item combined with a hopper.

- The last block should look like an open Laptop. It can be bound to a program/function. When right clicking it, it will open a text terminal that uses the same monospace font as the display block and also outputs the same thing as a display would. While the display is limited by the number of adjacent blocks, the terminal should have scroll bar. Below the display is a single input line that becomes available when an input is requested. This does not require items, just a user typing. A Laptop does not interact with WRITE. A Laptop can be crafted from a display block and an I/O block. It can also be crafted from a tablet item.

- The tablet is the mobile variant of a laptop. when having the item in hand, left clicking with it opens the dialog. A tablet can be crafted from a laptop block.

## 2026-10-24 – Feedback on I/O devices, PRINT/WRITE/INPUT as functions

1: Looks OK. The dialog should be wider to accomodate a program and function button, simitlar to the other blocks that bind to something. Increase both inventories to 18 slots. The layout of the trigger button is a good starting base for it.
2: The model looks OK but the OK button does not close the dialog. Please also make the wrench rotate it
3: Opening it works fine, the OK button does not work either
4: The items look good, rotating and camouflaging works but the block should contain a slot for the camouflage item similar to the trigger blocks. The dialog should match the trigger block design (light gray, a button for program, a button for function and one single inventory slot for camouflage.

Findings:
- When looking at the call button diagonally, I can see that the gray pixels of the call logo are not raised like the black and white ones.
- I can't enter a single space into a char literal, a space is treated as if nothing was entered.
- I created an assign statement without a trailing ; and the line number was yellow, because an end statement was not yet there. it should be red as the statement in that line is incomplete. Red errors should have a higher priority than yellow carried on from a previous statement.
- I created an INPUT statement for the Integer Anzahl. I had the laptop bound to the wrong program. I fixed that and got the prompt for Anzahl. When I typed 10 in the laptop, this did not accept the input but asked again.
-Please remove the dark blue lines on the VARIABLE block texture. It should look like a blue chest.
-From around 40 blockss distance, the camouflage texture mixes with the front of the display blocks
- Instead of statements, PRINT, INPUT and WRITE should become core functions that are used with CALL

## 2026-10-24 – WAITING state, chest seam, has_inventory

1: Both look OK
2: Both are fine now
3: Works as expected
4: Works as expected
5: The call symbol is fine, The border between chest lid and bottom is gone, too. That line should remain
6: Works fine

Another change: A process should get another state "WAITING" when waiting on an input and the cursor should pulse between black and dark green or black and dark red for the debug cursor.

The function minecraft/GET_BLOCK should also have a boolean property "has_inventory".

## 2026-10-24 – Cursor color, remaining steps

The chest is fine. The cursors brightest color should be lower than it is now, though. Please lower it and continue with the remaining steps of sprint 10

## 2026-10-24 – VM

the VM is now reachable again.

## 2026-10-24 – Modrinth

ssh -i .ssh/id_ed25519_claude doener@192.168.178.101 works for me. The doener user still has sudo rights without the need for a password

I have created the modrinth project and put the id into the TOML file and a PAT into the github secret MODRINTH_TOKEN.

## 2026-10-24 – Several clients

How can I start multiple clients with different users?

## 2026-10-24 – Operator

please make Player962 into an admin

## 2026-10-24 – Multiplayer results

Tests 1, 2 and 4 were successful on Fabric. Test 3 could be run by Tester2, allowing a non-operator to place and break blocks with a program written by Player962 and triggered by a trigger placed by that player.

I could not start test 5 with either the patchouly mod present or absent in the mods folder of the neoforge client. It complains about patchouli missing on the server.

Answer: Sheet owners (Recommended)

## 2026-10-25 – Fixed user names

As I restarted the client, The player in the first instance now has a different name. Can you switch the configs on the first fabrich and Neoforge instance to always usee Tester1 and make Tester1 an admin on both server instances?

## 2026-10-25 – Backups, findings, sheet info, run property, disk drive

Before you delete/redeploy a server, please make sure to backup all saved programs and restore them after you set the new server up.

Findings while testing with Fabric:
- The comment item in the inventory is not rotated,
- Test3: I created a program on sheet 3 on the fabric server with Tester1. Then I was able to run it as Tester2. I was also able to break blocks on that sheet.

Regarding Patchouli:
This should be optional. If patchouli is not installed on both, client and server, c0de should run fine but would not give out the book and the books would not be available.

Improvements:
-The x=0,z=0 block of a sheet should show the sheet number (x coordinate /32 fixed), its coordinates and owner. There should also be a way to set the sheets owner by clicking it with a wrench. A normal owner can give his own sheet to someone, an admin an give all sheets to someone, no matter who the owner is.
- The owner of the trigger should be able to set a property "Run", that can be "Only me", "Players", "Mobs" or "Everyone", restricting, if it should run at all. If a non-admin player runs a program, the restricted commands should cause a fatal error.
- When right clicking the program block or function block with the debug tool, the owner should be shown as well. When right clicking a PID, too
- Working with indexes led me to revert the decision of not showing an index when referencing a variable. The index should be shown on labels
- The clients .minecraft folder should contain a folder named c0de. When saving a program, a copy should go to that folder, similar to how schematics behave for the Create mod. Otherwise, saved programs are only accessible for someone with access to the world folder on the server.  Add a disk drive block that should look like a 3.5" disk drive in a case (e.g. like this https://cdn-reichelt.de/bilder/web/artikel_ws/E600%2FSANDBERG_133-50_01.jpg?type=Product&) with an inventory slot for a single disk, a button to save a program on the disk to the local folder and a dialog that lets you choose a program to load onto that disk when you click the load button.  That should replace the option of right clicking a program with a disk item.

Answers: Activator = Interaction only; Rights = The owner's rights (Recommended); Disk flow = Program on the disk (Recommended)

## 2026-10-25 – Feedback on owners, triggers, Patchouli

Regarding the comment item, it looks like \ instead of // in the inventory.
1: The text shows on the block above the sheet but you need to click the corner. This should be changed to clicking the block containing the text.
2: That works fine
3: Tester2 can click the buttons in the dialog  trigger block owned by player Tester1 Only when saving, they get a message "Not saved". The buttons and camouflage slot should be greyed out. Permissions work. Levers, buttons, pressure plates around the trigger in cardinal directions work.
4: I could load a program and save it.
5: Neoforge client 2 without Patchouli can connect to the server on port 25566 but not on the one on 25567, the same goes for Neoforge client 1, that has the Patchouli mod.  Client 1 gives the error message "Channel of mod "Patchouli" failed to connect: This channel is missing on the server side but required on the client side, when connecting to the server on port 25567

## 2026-10-25 – Trigger dialog layout, logo

All four tests are sucessful. I have noticed the error message in a trigger block for error 311 being written slightly over the caption "Camouflage". Suggestion: Put the camouflage inventory where  the checkmark button is, with its caption to the right. Put the checkmark button in the lower right corner and put the messages into a textbox with a scrollbar to prevent overflow.

Also, I have created a "icon.png" image.  Please crop that to a 256x256 pixel image and create a SVG logo based on that icon. It is essentially the comment object placed on the floor and viewed from a slight angle. I want to use that logo with Modrinth

## 2026-10-25 – Logo

The logo svg does not look like the image. There are spaces between the columns

## 2026-10-25 – Release

The logo is fine, you can now implement 11.13

## 2026-10-25 – Modrinth environment

Please make sure, when modifying versions to modrinth, always set the environment to "client and server"

## 2026-10-25 – Sprint 12

I did the change for the 0.1.0 versions manually as it was required by Modrinth to proceed with a review. Please continue with Sprint 12 and just make sure it is set for future releases.

## 2026-10-25 – Memory of the development runs

I have added the mods JEI and jade to both 1.21.11 instances. The game crashes/freezes on both due to an out of memory error when turning around and a lot of terrain is being generated. Van you increase Xms/Xmx settings for the gradle runs?

## 2026-10-25 – Commands in the 1.21.11 worlds

I ran into a freeze again and that disappeared when I disabled the mods. However, Tester1 does not seem to be an admin, so /give failed

## 2026-10-25 – Freeze of the 1.21.11 client

Even with all mods disabled and the world recreated, the fabric instanze freezes shortly after I enter the world and turn around. latest.log shows the logs for that last test.

ok, it just froze

## 2026-10-25 – In-game tests of 1.21.11

Both versions work fine now.
Back to the requested Tests for 1.21.11
1: Blocks and item in inventory look OK for both versions
2: Line numbers and sheet text shows up for both versions
3: Texts appear for both versions
5: The cursor pulses and marked lines are highlighted for both versions
6: The sky looks the same for both versions

## 2026-10-25 – Lighting in the c0de dimension on 26.x

The tests on 12.1 were done, I jut forgot to write them down here.
Findings so far:
- Upon entering the c0de dimension, the entire floor is black, the wrench in hand is black, too. It seems, there is no lighting. I've stopped testing there, as I could not find the beginning of sheet 0

## 2026-10-25 – Tests of 26.3

- The light level is fine now, a program was loaded and ran fine.
- Blocks look OK, line numbers look OK
- Display walls look OK, but depending on the angle, I can see a thin line of the camouflage when looking at the front of a display diagonally. However, when I run a program, I get no output on the display but it is fine on the laptop.
- Terminal keys are working
- The timer block dialogs are fine.

## 2026-10-25 – Displays on 26.3

The text now shows on the display and the line is gone.

## 2026-10-25 – Test servers, client GameTests

no servers should run permanently, just start them as needed for the tests.

Please continue with 12.4

## 2026-10-26 – Client GameTest 26.3 stuck in CI

Tve test got stuck at ClientGameTest 26.3 fabric: (log: Failed to create backend OpenGL: Couldn't find matching GLX visual; Failed to create backend Vulkan: Installed Vulkan doesn't implement the VK_KHR_surface extension)

## 2026-10-26 – Release

please go ahead

## 2026-10-26 – Release 0.2.0

please go on and do the release

## 2026-10-27 – Sprint 13

Let's resume previous session for minecraft-code, I have some new features I want to go into a new sprint:
First of all I encountered a problemwotking with a timer trigger: The initial delay 20 is too fast. As soon as it is configured, there is basically no chance to change anything, because timer always starts a new process faster than I can react.. To fix that, Timers should be disabled while receiving a redstone signal.

Then I have a few more changes I want to introduce:
- Disk drive: The dynamic texts, e.g. "Saved as" are cut off by the GUI elements. Reserve 2 proper lines in a text box for those messages and make the dialog a bit larger. Also, it would be cool if the drive makes a short sound when reading/writing to a disk, similar to the video https://www.youtube.com/watch?v=ZnFQZa8SKP8 after 7:30.
- Struct Sheet made available (ro)
- Struct PID made available (ro)
- Colors on screen should be supported, some pre defined constants should allow easy access
