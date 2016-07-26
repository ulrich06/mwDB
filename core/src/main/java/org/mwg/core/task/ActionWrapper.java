package org.mwg.core.task;

import org.mwg.plugin.AbstractTaskAction;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;

class ActionWrapper extends AbstractTaskAction {

    private final Action _wrapped;

    ActionWrapper(final Action p_wrapped) {
        super();
        this._wrapped = p_wrapped;
    }

    @Override
    public void eval(final TaskContext context) {
        //execute wrapped task but does not call the next method of the wrapped context
        //this allow to have exactly one call to the Context.next method
        _wrapped.eval(context);
    }

    @Override
    public String toString() {
        return "then()";
    }

}
