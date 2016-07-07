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
        Object prev = context.result();
        context.cleanObj(prev);
        context.setUnsafeResult(CoreTask.protect(context.graph(),context.variable(_name)));
        //continue for next step
    }

    @Override
    public String toString() {
        return "fromVar(\'" + _name + "\')";
    }

}
