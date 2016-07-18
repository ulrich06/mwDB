package org.mwg.core.task;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionWorld implements TaskAction {

    private final String _varName;

    ActionWorld(final String p_varName) {
        this._varName = p_varName;
    }

    @Override
    public void eval(final TaskContext context) {
        final String flat = context.template(_varName);
        context.setWorld(Long.parseLong(flat));
        context.continueTask();
    }

    @Override
    public String toString() {
        return "setWorld(\'" + _varName + "\')";
    }

}
