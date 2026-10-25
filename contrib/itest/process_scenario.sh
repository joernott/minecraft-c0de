#!/usr/bin/env bash
# Process scenario for the smoke test (task 8.8): builds a program on sheet 0 of the c0de dimension with
# commands, starts it with a trigger block (redstone) and a timer block and checks the results. A third
# process is running while the server restarts and must finish afterwards (task 8.7).
#
# Sourced by contrib/itest/smoke.sh, which provides rcon, logs and COMPOSE.

# The program, one block per entry: x z block[nbt]
#
#   PROGRAM itest
#   NOOP
#   FUNCTION run
#       PARAMETER value INTEGER
#       RESULT result INTEGER
#   BEGIN
#       FOR i INTEGER 1 ; 100 ;
#       BEGIN
#           ASSIGN result result + value ;
#       END FOR
#   END FUNCTION
PROGRAM_BLOCKS=(
    '5 0 c0de:program_block'
    '6 0 c0de:label_block{kind:"PROGRAM",parent:"",name:"itest",index:""}'
    '5 1 c0de:noop_block'
    '5 2 c0de:function_block'
    '6 2 c0de:label_block{kind:"FUNCTION",parent:"itest",name:"run",index:""}'
    '6 3 c0de:parameter_block'
    '7 3 c0de:label_block{kind:"PARAMETER",parent:"",name:"value",index:""}'
    '8 3 c0de:type_integer_block'
    '6 4 c0de:result_block'
    '7 4 c0de:label_block{kind:"RESULT",parent:"",name:"result",index:""}'
    '8 4 c0de:type_integer_block'
    '5 5 c0de:begin_block'
    '6 6 c0de:for_block'
    '7 6 c0de:label_block{kind:"VARIABLE",parent:"",name:"i",index:""}'
    '8 6 c0de:type_integer_block'
    '9 6 c0de:literal_block{type:"INTEGER",value:"1"}'
    '10 6 c0de:operator_block{operator:"END_OF_EXPRESSION"}'
    '11 6 c0de:literal_block{type:"INTEGER",value:"100"}'
    '12 6 c0de:operator_block{operator:"END_OF_EXPRESSION"}'
    '6 7 c0de:begin_block'
    '7 8 c0de:assign_block'
    '8 8 c0de:label_block{kind:"REFERENCE",parent:"",name:"result",index:""}'
    '9 8 c0de:label_block{kind:"REFERENCE",parent:"",name:"result",index:""}'
    '10 8 c0de:operator_block{operator:"ADD"}'
    '11 8 c0de:label_block{kind:"REFERENCE",parent:"",name:"value",index:""}'
    '12 8 c0de:operator_block{operator:"END_OF_EXPRESSION"}'
    '6 9 c0de:end_block'
    '7 9 c0de:for_block'
    '5 10 c0de:end_block'
    '6 10 c0de:function_block'
)

# A trigger block at x z in the black area of the c0de dimension, configured for the program, with the input
# value 5. The result of the program is 500.
place_trigger() {
    local service="$1" block="$2" x="$3" z="$4"
    rcon "$service" "execute in c0de:c0de run setblock $x -3 $z c0de:$block{program:\"itest\",function:\"run\",interval:20,input:[{Slot:0b,id:\"c0de:variable_item\",count:1,components:{\"minecraft:custom_data\":{name:\"value\",type:\"INTEGER\",value:\"5\"}}}]}" >/dev/null
}

# Waits until the output inventory of the trigger block at x z contains the result 500
wait_for_result() {
    local service="$1" x="$2" z="$3" seconds="$4"
    local deadline=$((SECONDS + seconds))
    while [ $SECONDS -lt $deadline ]; do
        if grep -q 'value: "500"' <<<"$(rcon "$service" "execute in c0de:c0de run data get block $x -3 $z output")"; then
            return 0
        fi
        sleep 2
    done
    return 1
}

process_scenario() {
    local service="$1"
    # Both chunks of sheet 0 must be loaded, so that the program is validated and registered
    rcon "$service" "execute in c0de:c0de run forceload add 0 0 31 31" >/dev/null
    for entry in "${PROGRAM_BLOCKS[@]}"; do
        read -r x z block <<<"$entry"
        rcon "$service" "execute in c0de:c0de run setblock $x -3 $z $block" >/dev/null
    done
    # The validation runs a few ticks after the blocks are placed; a server that just started may lag behind
    local valid=0
    for _ in $(seq 1 15); do
        sleep 2
        if grep -q "Test passed" <<<"$(rcon "$service" "execute in c0de:c0de if block 5 -3 10 c0de:end_block[valid=true]")"; then
            valid=1
            break
        fi
    done
    if [ $valid = 0 ]; then
        echo "::error title=Process scenario::The program on $service is not valid"
        return 1
    fi
    echo "program valid"

    place_trigger "$service" trigger_block -3 -3
    rcon "$service" "execute in c0de:c0de run setblock -3 -3 -2 minecraft:redstone_block" >/dev/null
    place_trigger "$service" timer_block -6 -3
    if ! wait_for_result "$service" -3 -3 60; then
        echo "::error title=Process scenario::The process started by redstone on $service returned no result"
        return 1
    fi
    echo "trigger block ok"
    if ! wait_for_result "$service" -6 -3 60; then
        echo "::error title=Process scenario::The process started by the timer on $service returned no result"
        return 1
    fi
    echo "timer block ok"

    # A process that is still running when the server restarts continues afterwards
    place_trigger "$service" trigger_block -9 -3
    rcon "$service" "execute in c0de:c0de run setblock -9 -3 -2 minecraft:redstone_block" >/dev/null
    sleep 1
    "${COMPOSE[@]}" restart "$service" >/dev/null 2>&1
    local deadline=$((SECONDS + TIMEOUT_SECONDS))
    until [ "$(grep -c 'Done ([0-9.]*s)! For help' <<<"$(logs "$service")")" -ge 2 ]; do
        if [ $SECONDS -ge $deadline ]; then
            echo "::error title=Process scenario::$service did not restart"
            return 1
        fi
        sleep 5
    done
    if ! wait_for_result "$service" -9 -3 60; then
        echo "::error title=Process scenario::The process on $service didn't continue after the restart"
        return 1
    fi
    echo "process survived the restart"
}
