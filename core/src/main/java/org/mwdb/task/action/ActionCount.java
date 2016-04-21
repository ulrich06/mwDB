package org.mwdb.task.action;

import org.mwdb.KTaskAction;
import org.mwdb.KTaskContext;
import org.mwdb.utility.PrimitiveHelper;

public class ActionCount implements KTaskAction {

    @Override
    public void eval(final KTaskContext context) {
        final Object previousResult = context.getPreviousResult();
        if (previousResult != null) {
            context.setResult(PrimitiveHelper.arraySize(previousResult));
        } else {
            context.setResult(0);
        }
        //continue for next step
        context.next();
    }

}
