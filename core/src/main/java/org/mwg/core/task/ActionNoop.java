package org.mwg.core.task;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionNoop implements TaskAction {

    @Override
    public void eval(final TaskContext context) {
        final Object previousResult = context.getPreviousResult();
        context.setResult(previousResult);
        //continue for next step
        context.next();
    }

}
