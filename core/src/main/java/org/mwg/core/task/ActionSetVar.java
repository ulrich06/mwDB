package org.mwg.core.task;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionSetVar implements TaskAction {

    private final String _name;

    private final Object _value;

    ActionSetVar(final String name, final Object value) {
        this._name = name;
        this._value = value;
    }

    @Override
    public void eval(final TaskContext context) {
        context.setVariable(this._name, this._value);
        context.setResult(context.result());//trigger next step
    }
}
