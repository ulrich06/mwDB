package org.mwg.core.task;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionSplit implements TaskAction {

    private String _splitPattern;

    public ActionSplit(String p_splitPattern) {
        this._splitPattern = p_splitPattern;
    }

    @Override
    public void eval(TaskContext context) {
        Object previous = context.result();
        if (previous != null) {
            String flat = context.resultAsString();
            String[] nextRes = flat.split(context.template(this._splitPattern));
            context.cleanObj(previous);
            context.setUnsafeResult(nextRes);
        } else {
            context.setUnsafeResult(new String[0]);
        }
    }

    @Override
    public String toString() {
        return "split(\'" + _splitPattern + "\')";
    }

}
