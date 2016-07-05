package org.mwg.core.task;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionTime implements TaskAction {

    private final String _varName;

    ActionTime(final String p_varName) {
        this._varName = p_varName;
    }

    @Override
    public void eval(final TaskContext context) {
        String flat = CoreTask.template(_varName, context);
        context.setTime(parse(flat));
        context.setUnsafeResult(context.result());
    }

    /**
     * @native ts
     * return parseInt(flat);
     */
    private long parse(String flat) {
        return Long.parseLong(flat);
    }

}

