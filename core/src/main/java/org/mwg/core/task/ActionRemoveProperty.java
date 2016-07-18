package org.mwg.core.task;

import org.mwg.Node;
import org.mwg.plugin.AbstractNode;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

class ActionRemoveProperty implements TaskAction {

    private final String _propertyName;

    ActionRemoveProperty(String propertyName) {
        this._propertyName = propertyName;
    }

    @Override
    public void eval(TaskContext context) {
        final TaskResult previousResult = context.result();
        if (previousResult != null) {
            final String flatRelationName = context.template(_propertyName);
            for (int i = 0; i < previousResult.size(); i++) {
                Object loopObj = previousResult.get(i);
                if (loopObj instanceof AbstractNode) {
                    Node loopNode = (Node) loopObj;
                    loopNode.removeProperty(flatRelationName);
                }
            }
        }
        context.continueTask();
    }

    @Override
    public String toString() {
        return "removeProperty(\'" + _propertyName + "\')";
    }


}
