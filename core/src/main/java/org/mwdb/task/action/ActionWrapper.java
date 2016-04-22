package org.mwdb.task.action;

import org.mwdb.KTaskAction;
import org.mwdb.KTaskContext;
import org.mwdb.task.TaskContextWrapper;

public class ActionWrapper implements KTaskAction {

    private final KTaskAction _wrapped;

    public ActionWrapper(final KTaskAction p_wrapped) {
        _wrapped = p_wrapped;
    }

    @Override
    public void eval(KTaskContext context) {
        //execute wrapped task but protect the next method
        _wrapped.eval(new TaskContextWrapper(context));
        context.next();
    }

}
