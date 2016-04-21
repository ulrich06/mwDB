package org.mwdb.task.action;

import org.mwdb.KTaskAction;
import org.mwdb.KTaskContext;

public class ActionTime implements KTaskAction {

    private final long _time;

    public ActionTime(final long p_time) {
        this._time = p_time;
    }

    @Override
    public void eval(final KTaskContext context) {
        context.setTime(_time);
        context.setResult(context.getPreviousResult());
        context.next();
    }

}
