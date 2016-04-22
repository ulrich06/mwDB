package org.mwdb.task.action;

import org.mwdb.KTask;
import org.mwdb.KTaskAction;
import org.mwdb.KTaskContext;

public class ActionSub implements KTaskAction {

    private final KTask _subTask;

    public ActionSub(final KTask p_subTask) {
        _subTask = p_subTask;
    }

    @Override
    public void eval(KTaskContext context) {
        _subTask.executeThenAsync(context, context.getPreviousResult(), new KTaskAction() {
            @Override
            public void eval(KTaskContext subTaskFinalContext) {
                context.setResult(subTaskFinalContext);
                context.next();
            }
        });
    }

}
