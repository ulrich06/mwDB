package org.mwg.core.task;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

class ActionFromVar implements TaskAction {

    private final String _name;

    ActionFromVar(final String p_name) {
        this._name = p_name;
    }

    @Override
    public void eval(final TaskContext context) {
        final String evaluatedName = context.template(_name);
        final TaskResult varResult = context.variable(evaluatedName);
        context.continueWith(varResult.clone());
    }

    @Override
    public String toString() {
        return "fromVar(\'" + _name + "\')";
    }

}
