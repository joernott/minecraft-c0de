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
