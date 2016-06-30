package org.mwg.core.task;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionPrintln implements TaskAction {

    @Override
    public void eval(final TaskContext context) {
        Object result = context.result();
        System.out.println(result);
        context.setResult(result);
        context.next();
    }

}
