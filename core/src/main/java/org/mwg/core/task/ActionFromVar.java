package org.mwg.core.task;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionFromVar implements TaskAction {

    private final String _name;

    ActionFromVar(final String p_name) {
        this._name = p_name;
    }

    @Override
    public void eval(final TaskContext context) {
        context.setResult(context.variable(_name));
        //continue for next step
    }

    @Override
    public String toString() {
        return "fromVar(\'" + _name + "\')";
    }

}
