package org.mwdb.task.action;

import org.mwdb.KTaskAction;
import org.mwdb.KTaskContext;

public class ActionNoop implements KTaskAction {

    @Override
    public void eval(final KTaskContext context) {
        final Object previousResult = context.getPreviousResult();
        context.setResult(previousResult);
        //continue for next step
        context.next();
    }

}
