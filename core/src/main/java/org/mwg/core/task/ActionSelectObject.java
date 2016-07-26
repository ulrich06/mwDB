package org.mwg.core.task;

import org.mwg.Node;
import org.mwg.plugin.AbstractNode;
import org.mwg.plugin.AbstractTaskAction;
import org.mwg.task.*;

class ActionSelectObject extends AbstractTaskAction {

    private final TaskFunctionSelectObject _filter;

    ActionSelectObject(final TaskFunctionSelectObject filterFunction) {
        super();
        this._filter = filterFunction;
    }

    @Override
    public void eval(final TaskContext context) {
        final TaskResult previous = context.result();
        final TaskResult next = context.wrap(null);
        final TaskResultIterator iterator = previous.iterator();
        Object nextElem = iterator.next();
        while(nextElem != null) {
            if(_filter.select(nextElem,context)) {
                if(nextElem instanceof AbstractNode) {
                    Node casted = (Node) nextElem;
                    next.add(casted.graph().cloneNode(casted));
                } else {
                    next.add(nextElem);
                }
            }
            nextElem = iterator.next();
        }
        context.continueWith(next);
    }

    @Override
    public String toString() {
        return "selectObject()";
    }
}
