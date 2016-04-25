package org.mwg.core.task;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;
import org.mwg.core.task.TaskContextWrapper;

class ActionWrapper implements TaskAction {

    private final TaskAction _wrapped;

    ActionWrapper(final TaskAction p_wrapped) {
        _wrapped = p_wrapped;
    }

    @Override
    public void eval(TaskContext context) {
        //execute wrapped task but protect the next method
        _wrapped.eval(new TaskContextWrapper(context));
        context.next();
    }

}
