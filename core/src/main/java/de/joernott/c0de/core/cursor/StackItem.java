package de.joernott.c0de.core.cursor;

import de.joernott.c0de.core.eval.Variable;
import de.joernott.c0de.core.program.Statement;
import de.joernott.c0de.core.value.Value;
import java.util.ArrayList;
import java.util.List;

/** An item on the stack of a cursor (see docs/concept/15-cursor.md, "The stack"). */
public sealed interface StackItem {
    /**
     * A variable or constant, including parameters, results and loop variables.
     *
     * @param global true if it was declared in the global scope
     */
    record VariableItem(Variable variable, boolean global) implements StackItem {}

    /**
     * A function item (call frame).
     *
     * @param function the name of the function
     * @param entry true for the entry function of the process
     * @param functionLine z coordinate of the FUNCTION line (for the visibility of global variables)
     * @param call the CALL statement, or null for the entry function
     * @param returnLine z coordinate where the cursor continues after the function, or -1
     * @param results the result variables of the function
     */
    record FunctionFrame(
            String function,
            boolean entry,
            int functionLine,
            Statement.Call call,
            int returnLine,
            List<Variable> results)
            implements StackItem {
        public FunctionFrame {
            results = new ArrayList<>(results);
        }
    }

    /**
     * A block marker for an open code block.
     *
     * @param owner the statement the code block belongs to
     * @param block the code block
     */
    record BlockMarker(Statement owner, Statement.CodeBlock block) implements StackItem {}

    /** The state of a running loop. It is below the loop variable and the block marker of the loop body. */
    final class LoopItem implements StackItem {
        private final Statement loop;
        private final Value end;
        private final Value step;
        private final List<Value> remaining;

        /**
         * @param loop the FOR, EACH, WHILE or REPEAT statement
         * @param end the end value of a FOR loop, or null
         * @param step the increment of a FOR loop, or null
         * @param remaining the elements an EACH loop has not processed yet, or an empty list
         */
        public LoopItem(Statement loop, Value end, Value step, List<Value> remaining) {
            this.loop = loop;
            this.end = end;
            this.step = step;
            this.remaining = new ArrayList<>(remaining);
        }

        public Statement loop() {
            return loop;
        }

        public Value end() {
            return end;
        }

        public Value step() {
            return step;
        }

        /** The remaining elements of an EACH loop; the list is mutable. */
        public List<Value> remaining() {
            return remaining;
        }
    }
}
