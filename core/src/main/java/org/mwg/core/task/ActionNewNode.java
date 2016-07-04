package org.mwg.core.task;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionNewNode implements TaskAction {

    ActionNewNode() {
    }

    @Override
    public void eval(TaskContext context) {
        context.setResult(context.graph().newNode(context.world(), context.time()));
    }
}
