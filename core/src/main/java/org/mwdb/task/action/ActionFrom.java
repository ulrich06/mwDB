package org.mwdb.task.action;

import org.mwdb.KTaskAction;
import org.mwdb.KTaskContext;

public class ActionFrom implements KTaskAction {

    private final String _name;

    public ActionFrom(final String p_name) {
        this._name = p_name;
    }

    @Override
    public void eval(final KTaskContext context) {
        context.setResult(context.getVariable(_name));
        //continue for next step
        context.next();
    }

}
