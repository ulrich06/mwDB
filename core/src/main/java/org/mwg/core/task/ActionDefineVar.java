package org.mwg.core.task;

import org.mwg.plugin.AbstractTaskAction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

class ActionDefineVar extends AbstractTaskAction {

    private final String _name;

    ActionDefineVar(final String p_name) {
        super();
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
        return "defineVar(\'" + _name + "\')";
    }

}
