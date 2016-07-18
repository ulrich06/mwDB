package org.mwg.core.task;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

class ActionInject implements TaskAction {

    private final Object _value;

    ActionInject(final Object value) {
        this._value = value;
    }

    @Override
    public void eval(final TaskContext context) {
        context.continueWith(context.wrap(_value));
    }

    @Override
    public String toString() {
        return "inject()";
    }

}
