package org.mwg.core.task;

import org.mwg.plugin.AbstractTaskAction;
import org.mwg.task.TaskContext;

class ActionClear extends AbstractTaskAction {

    @Override
    public void eval(final TaskContext context) {
        context.continueWith(context.newResult());
    }

    @Override
    public String toString() {
        return "clear()";
    }

}
