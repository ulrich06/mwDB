package org.mwg.core.task;

import org.mwg.Node;
import org.mwg.plugin.AbstractNode;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskFunctionSelectObject;
import org.mwg.task.TaskResult;

class ActionSelectObject implements TaskAction {

    private final TaskFunctionSelectObject _filter;

    ActionSelectObject(TaskFunctionSelectObject filterFunction) {
        _filter = filterFunction;
    }

    @Override
    public void eval(TaskContext context) {
        final TaskResult previous = context.result();
        final TaskResult next = context.wrap(null);
        final int previousSize = previous.size();
        for (int i = 0; i < previousSize; i++) {
            final Object obj = previous.get(i);
            if (_filter.select(obj, context)) {
                if (obj instanceof AbstractNode) {
                    Node casted = (Node) obj;
                    next.add(casted.graph().cloneNode(casted));
                } else {
                    next.add(obj);
                }
            }
        }
        context.continueWith(next);
    }

    @Override
    public String toString() {
        return "selectObject()";
    }
}
