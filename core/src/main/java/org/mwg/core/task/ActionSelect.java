package org.mwg.core.task;

import org.mwg.Node;
import org.mwg.plugin.AbstractNode;
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
        final Object previousResult = context.result();
        if (previousResult != null) {
            if (previousResult instanceof Object[]) {
                context.setResult(filterArray((Object[]) previousResult));
            } else if (previousResult instanceof AbstractNode) {
                if (_filter.select((Node) previousResult)) {
                    context.setResult(previousResult);
                } else {
                    context.setResult(new Node[0]);
                }
            } else {
                context.setResult(previousResult); //no map transform
            }
        }
        context.next();
    }

    private Object[] filterArray(Object[] current) {
        boolean onlyContainsNodes = true;
        Object[] filteredResult = new Object[current.length];
        int cursor = 0;
        for (int i = 0; i < current.length; i++) {
            if (current[i] instanceof Object[]) {
                onlyContainsNodes = false;
                Object[] filtered = filterArray((Object[]) current[i]);
                if (filtered != null && filtered.length > 0) {
                    filteredResult[cursor] = filtered;
                    cursor++;
                }
            } else if (current[i] != null && current[i] instanceof AbstractNode) {
                if (_filter.select((Node) current[i])) {
                    filteredResult[cursor] = current[i];
                    cursor++;
                }
            } else {
                onlyContainsNodes = false;
                filteredResult[cursor] = current[i];
                cursor++;
            }
        }
        if (onlyContainsNodes) {
            Node[] finalNodes = new Node[cursor];
            System.arraycopy(filteredResult, 0, finalNodes, 0, cursor);
            return finalNodes;
        } else {
            if (cursor == filteredResult.length) {
                return filteredResult;
            } else {
                Object[] shrinkedResult = new Object[cursor];
                System.arraycopy(filteredResult, 0, shrinkedResult, 0, cursor);
                return shrinkedResult;
            }
        }
    }


}
