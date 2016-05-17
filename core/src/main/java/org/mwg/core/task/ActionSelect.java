package org.mwg.core.task;

import org.mwg.Node;
import org.mwg.plugin.AbstractNode;
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
            if (previousResult instanceof AbstractNode[]) {
                context.setResult(filterNodeArray((AbstractNode[]) previousResult));
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
            if (current[i] instanceof AbstractNode[]) {
                Node[] filteredNodes = filterNodeArray((AbstractNode[]) current[i]);
                if (filteredNodes != null && filteredNodes.length > 0) {
                    filteredResult[cursor] = filteredNodes;
                    cursor++;
                }
            } else if (current[i] instanceof Object[]) {
                Object[] filteredObjs = filterArray((Object[]) current[i]);
                if (filteredObjs != null && filteredObjs.length > 0) {
                    filteredResult[cursor] = filteredObjs;
                    cursor++;
                }
            }
            if (current[i] != null && current[i] instanceof AbstractNode) {
                if (_filter.select((Node) current[i])) {
                    filteredResult[cursor] = current[i];
                    cursor++;
                }
            }
        }
        return filteredResult;
    }

    private Node[] filterNodeArray(Node[] current) {
        Node[] filtered = new AbstractNode[current.length];
        int cursor = 0;
        for (int i = 0; i < current.length; i++) {
            if (current[i] != null && _filter.select(current[i])) {
                filtered[cursor] = current[i];
                cursor++;
            }
        }
        if (cursor != current.length) {
            Node[] filtered_2 = new AbstractNode[cursor];
            System.arraycopy(filtered, 0, filtered_2, 0, cursor);
            return filtered_2;
        } else {
            return filtered;
        }
    }


}
