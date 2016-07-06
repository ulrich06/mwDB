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
        String flat = context.template(_varName);
        context.setWorld(parse(flat));
        context.setUnsafeResult(context.result());
    }

    /**
     * @native ts
     * return parseInt(flat);
     */
    private long parse(String flat) {
        return Long.parseLong(flat);
    }

    @Override
    public String toString() {
        return "setWorld(" + _varName + ")";
    }

}
