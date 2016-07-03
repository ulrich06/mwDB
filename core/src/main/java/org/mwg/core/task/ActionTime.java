package org.mwg.core.task;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionTime implements TaskAction {

    private final long _time;

    ActionTime(final long p_time) {
        this._time = p_time;
    }

    @Override
    public void eval(final TaskContext context) {
        context.setTime(_time);
        context.setUnsafeResult(context.result());
    }

}
