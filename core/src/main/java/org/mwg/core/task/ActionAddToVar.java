package org.mwg.core.task;

import org.mwg.plugin.AbstractTaskAction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

class ActionAddToVar extends AbstractTaskAction {

    private final String _name;
    private final boolean _global;

    ActionAddToVar(final String p_name, final boolean p_global) {
        super();
        this._name = p_name;
        this._global = p_global;
    }

    @Override
    public void eval(final TaskContext context) {
        final TaskResult previousResult = context.result();
        if (_global) {
            context.addToGlobalVariable(context.template(_name), previousResult);
        } else {
            context.addToLocalVariable(context.template(_name), previousResult);
        }
        context.continueTask();
    }

    @Override
    public String toString() {
        if (_global) {
            return "addToGlobalVar(\'" + _name + "\')";
        } else {
            return "addToLocalVar(\'" + _name + "\')";
        }
    }

}
