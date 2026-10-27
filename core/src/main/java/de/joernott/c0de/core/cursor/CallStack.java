package de.joernott.c0de.core.cursor;

import de.joernott.c0de.core.cursor.StackItem.FunctionFrame;
import de.joernott.c0de.core.cursor.StackItem.VariableItem;
import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.eval.Scope;
import de.joernott.c0de.core.eval.Variable;
import de.joernott.c0de.core.value.Value;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * The stack of a cursor. It resolves names lexically: the variables of the current function (from the top
 * down to the function item), then the global variables declared before the current function, then the
 * constants that are computed when they are read (CURSOR), the constants of included libraries and the built-in
 * constants.
 */
final class CallStack implements Scope {
    private final List<StackItem> items = new ArrayList<>();
    private final int limit;
    private final Map<String, Value> constants;
    private final Map<String, Supplier<Value>> computed;

    /**
     * @param limit the number of slots
     * @param constants the constants of libraries (with qualified names) and the built-in constants
     * @param computed the constants whose values are computed when they are read
     */
    CallStack(int limit, Map<String, Value> constants, Map<String, Supplier<Value>> computed) {
        this.limit = limit;
        this.constants = constants;
        this.computed = computed;
    }

    /** Pushes an item. Crashes with STACK_OVERFLOW if the stack is full. */
    void push(StackItem item) {
        if (items.size() >= limit) {
            throw C0deException.of(ErrorCode.STACK_OVERFLOW, "The stack is full (" + limit + " slots)");
        }
        items.add(item);
    }

    /** Removes items from the top down to and including the first item that matches. */
    StackItem popTo(Predicate<StackItem> match) {
        while (!items.isEmpty()) {
            StackItem item = items.remove(items.size() - 1);
            if (match.test(item)) {
                return item;
            }
        }
        throw new IllegalStateException("The item to pop to is not on the stack");
    }

    StackItem pop() {
        return items.remove(items.size() - 1);
    }

    StackItem top() {
        return items.isEmpty() ? null : items.get(items.size() - 1);
    }

    /** The topmost function item, or null in the global scope. */
    FunctionFrame currentFrame() {
        for (int i = items.size() - 1; i >= 0; i--) {
            if (items.get(i) instanceof FunctionFrame frame) {
                return frame;
            }
        }
        return null;
    }

    /** The function item of the entry function, or null. */
    FunctionFrame entryFrame() {
        for (StackItem item : items) {
            if (item instanceof FunctionFrame frame && frame.entry()) {
                return frame;
            }
        }
        return null;
    }

    /** The topmost item matching a condition above the current function item, or null. */
    <T extends StackItem> T findInFunction(Class<T> type) {
        for (int i = items.size() - 1; i >= 0; i--) {
            StackItem item = items.get(i);
            if (item instanceof FunctionFrame) {
                return null;
            }
            if (type.isInstance(item)) {
                return type.cast(item);
            }
        }
        return null;
    }

    int size() {
        return items.size();
    }

    /** The number of slots. */
    int limit() {
        return limit;
    }

    /** The number of function items: the depth of the calls. */
    int depth() {
        return (int) items.stream().filter(FunctionFrame.class::isInstance).count();
    }

    List<StackItem> items() {
        return items;
    }

    @Override
    public Optional<Variable> find(String name) {
        FunctionFrame frame = null;
        for (int i = items.size() - 1; i >= 0; i--) {
            StackItem item = items.get(i);
            if (item instanceof FunctionFrame f) {
                frame = f;
                break;
            }
            if (item instanceof VariableItem variable
                    && variable.variable().name().equals(name)) {
                return Optional.of(variable.variable());
            }
        }
        int visibleBefore = frame == null ? Integer.MAX_VALUE : frame.functionLine();
        for (int i = items.size() - 1; i >= 0; i--) {
            if (items.get(i) instanceof VariableItem variable
                    && variable.global()
                    && variable.variable().line() < visibleBefore
                    && variable.variable().name().equals(name)) {
                return Optional.of(variable.variable());
            }
        }
        Supplier<Value> value = computed.get(name);
        Value constant = value != null ? value.get() : constants.get(name);
        if (constant != null) {
            return Optional.of(new Variable(name, constant.type(), constant, true, Map.of()));
        }
        return Optional.empty();
    }
}
