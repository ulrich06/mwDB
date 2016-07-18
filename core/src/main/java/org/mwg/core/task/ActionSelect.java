package org.mwg.core.task;

import org.mwg.Node;
import org.mwg.plugin.AbstractNode;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskFunctionSelect;
import org.mwg.task.TaskResult;

class ActionSelect implements TaskAction {

    private final TaskFunctionSelect _filter;

    ActionSelect(final TaskFunctionSelect p_filter) {
        this._filter = p_filter;
    }

    @Override
    public final void eval(final TaskContext context) {
        final TaskResult previous = context.result();
        final TaskResult next = context.wrap(null);
        final int previousSize = previous.size();
        for (int i = 0; i < previousSize; i++) {
            final Object obj = previous.get(i);
            if (obj instanceof AbstractNode) {
                final Node casted = (Node) obj;
                if (_filter.select(casted, context)) {
                    next.add(casted.graph().cloneNode(casted));
                }
            } else {
                next.add(obj);
            }
        }
        context.continueWith(next);
    }

    @Override
    public String toString() {
        return "select()";
    }

}
