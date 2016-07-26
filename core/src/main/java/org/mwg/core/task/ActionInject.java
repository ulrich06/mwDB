package org.mwg.core.task;

import org.mwg.plugin.AbstractTaskAction;
import org.mwg.task.TaskContext;

class ActionInject extends AbstractTaskAction {

    private final Object _value;

    ActionInject(final Object value) {
        super();
        this._value = value;
    }

    @Override
    public void eval(final TaskContext context) {
        context.continueWith(context.wrap(_value).clone());
    }

    @Override
    public String toString() {
        return "inject()";
    }

}
