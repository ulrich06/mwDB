package org.mwg.core.task;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionAsVar implements TaskAction {

    private final String _name;

    ActionAsVar(final String p_name) {
        this._name = p_name;
    }

    @Override
    public void eval(final TaskContext context) {
        final Object previousResult = context.result();
        context.setVariable(_name, previousResult);
        context.setUnsafeResult(previousResult);
    }

    @Override
    public String toString() {
        return "add(\'" + _name + "\')";
    }


}
