package org.mwg.core.task;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionAsVar implements TaskAction {

    private final String _name;

    ActionAsVar(final String p_name) {
        this._name = p_name;
    }

    @Override
    public void eval(final TaskContext context) {
        final Object previousResult = context.getPreviousResult();
        context.setResult(previousResult);
        context.setVariable(_name, previousResult);
        //continue for next step
        context.next();
    }

}
