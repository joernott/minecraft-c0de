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
