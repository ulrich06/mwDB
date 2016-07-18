package org.mwg.core.task;

import org.mwg.Node;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

class ActionNewNode implements TaskAction {

    private final String _typeNode;

    ActionNewNode(final String typeNode) {
        this._typeNode = typeNode;
    }

    @Override
    public void eval(TaskContext context) {
        Node newNode;
        if (_typeNode == null) {
            newNode = context.graph().newNode(context.world(), context.time());
        } else {
            String templatedType = context.template(_typeNode);
            newNode = context.graph().newTypedNode(context.world(), context.time(), templatedType);
        }
        context.continueWith(context.wrap(newNode));
    }

    @Override
    public String toString() {
        if (_typeNode != null) {
            return "newTypedNode(\'" + _typeNode + "\')";
        }
        return "newNode()";
    }

}
