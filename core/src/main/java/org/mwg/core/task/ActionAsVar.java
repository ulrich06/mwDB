package org.mwg.core.task;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

class ActionAsVar implements TaskAction {

    private final String _name;

    ActionAsVar(final String p_name) {
        this._name = p_name;
    }

    @Override
    public void eval(final TaskContext context) {
        final TaskResult previousResult = context.result();
        context.setVariable(context.template(_name), previousResult);
        context.continueTask();
    }

    @Override
    public String toString() {
        return "asVar(\'" + _name + "\')";
    }


}
