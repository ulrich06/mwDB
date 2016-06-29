package org.mwg.core.task;

import org.mwg.task.Action;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionWrapper implements TaskAction {

    private final Action _wrapped;

    private final boolean _syncProtection;

    ActionWrapper(final Action p_wrapped, boolean p_syncProtection) {
        _wrapped = p_wrapped;
        _syncProtection = p_syncProtection;
    }

    @Override
    public void eval(TaskContext context) {
        //execute wrapped task but does not call the next method of the wrapped context
        //this allow to have exactly one call to the Context.next method
        if (_syncProtection) {
            _wrapped.eval(new TaskContextWrapper(context));
            context.next();
        } else {
            _wrapped.eval(context);
        }
    }

}
