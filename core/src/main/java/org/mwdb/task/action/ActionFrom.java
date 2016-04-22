package org.mwdb.task.action;

import org.mwdb.KTaskAction;
import org.mwdb.KTaskContext;

public class ActionFrom implements KTaskAction {

    private final Object _value;

    public ActionFrom(final Object value) {
        this._value = value;
    }

    @Override
    public void eval(final KTaskContext context) {
        context.setResult(_value);
        context.next();
    }

}
