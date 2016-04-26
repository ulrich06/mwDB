package org.mwg.core.task;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionWrapper implements TaskAction {

    private final TaskAction _wrapped;

    ActionWrapper(final TaskAction p_wrapped) {
        _wrapped = p_wrapped;
    }

    @Override
    public void eval(TaskContext context) {
        //execute wrapped task but does not call the next method of the wrapped context
        //this allow to have exactly one call to the Context.next method
        _wrapped.eval(new TaskContextWrapper(context));
        context.next();
    }

}
