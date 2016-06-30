package org.mwg.core.task;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionFrom implements TaskAction {

    private final Object _value;

    ActionFrom(final Object value) {
        this._value = value;
    }

    @Override
    public void eval(final TaskContext context) {
        context.setResult(CoreTask.protect(context.graph(),_value));
        context.next();
    }

}
