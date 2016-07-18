package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.Constants;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionSave implements TaskAction {

    @Override
    public void eval(final TaskContext context) {

        /*
        context.graph().save(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                context.setUnsafeResult(context.result());
            }
        });
        */
    }

    @Override
    public String toString() {
        return "save()";
    }

}
