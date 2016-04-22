package org.mwdb.task.action;

import org.mwdb.KTaskAction;
import org.mwdb.KTaskContext;

public class ActionAsVar implements KTaskAction {

    private final String _name;

    public ActionAsVar(final String p_name) {
        this._name = p_name;
    }

    @Override
    public void eval(final KTaskContext context) {
        final Object previousResult = context.getPreviousResult();
        context.setResult(previousResult);
        context.setVariable(_name, previousResult);
        //continue for next step
        context.next();
    }

}
