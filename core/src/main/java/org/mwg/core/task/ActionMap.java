package org.mwg.core.task;

import org.mwg.Node;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskFunctionMap;

class ActionMap implements TaskAction {

    private final TaskFunctionMap _map;

    ActionMap(final TaskFunctionMap p_map) {
        this._map = p_map;
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
                Object[] filtered = filterNodeArray((Node[]) current[i]);
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
                filteredResult[cursor] = _map.map((Node) current[i]);
                cursor++;
            }
        }
        return filteredResult;
    }

    private Object[] filterNodeArray(Node[] current) {
        Object[] filtered = new Object[current.length];
        int cursor = 0;
        for (int i = 0; i < current.length; i++) {
            filtered[cursor] = _map.map(current[i]);
            cursor++;
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
