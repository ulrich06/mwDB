package org.mwg.core.task;

import org.mwg.Node;
import org.mwg.task.Task;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskFunctionSelect;

class ActionSelect implements TaskAction {

    private final TaskFunctionSelect _filter;

    ActionSelect(final TaskFunctionSelect p_filter) {
        this._filter = p_filter;
    }

    @Override
    public final void eval(final TaskContext context) {
        final Object previousResult = context.getPreviousResult();
        if (previousResult != null) {
            if (previousResult instanceof Node[]) {
                context.setResult(filterNodeArray((Node[]) previousResult));
            } else if (previousResult instanceof Object[]) {
                context.setResult(filterArray((Object[]) previousResult));
            }
        }
        context.next();
    }

    private Object[] filterArray(Object[] current) {
        Object[] filteredResult = new Object[current.length];
        int cursor = 0;
        for (int i = 0; i < current.length; i++) {
            if (current[i] instanceof Node[]) {
                Node[] filtered = filterNodeArray((Node[]) current[i]);
                if (filtered != null && filtered.length > 0) {
                    filteredResult[cursor] = filtered;
                    cursor++;
                }
            } else if (current[i] instanceof Object[]) {
                Object[] filtered = filterArray((Object[]) current[i]);
                if (filtered != null && filtered.length > 0) {
                    filteredResult[cursor] = filtered;
                    cursor++;
                }
            }
            if (current[i] != null && current[i] instanceof Node) {
                if (_filter.select((Node) current[i])) {
                    filteredResult[cursor] = current[i];
                    cursor++;
                }
            }
        }
        return filteredResult;
    }

    private Node[] filterNodeArray(Node[] current) {
        Node[] filtered = new Node[current.length];
        int cursor = 0;
        for (int i = 0; i < current.length; i++) {
            if (current[i] != null && _filter.select(current[i])) {
                filtered[cursor] = current[i];
                cursor++;
            }
        }
        if (cursor != current.length) {
            Node[] filtered_2 = new Node[cursor];
            System.arraycopy(filtered, 0, filtered_2, 0, cursor);
            return filtered_2;
        } else {
            return filtered;
        }
    }


}
