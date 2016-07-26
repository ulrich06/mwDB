package org.mwg.plugin;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

public abstract class AbstractTaskAction implements TaskAction {

    private AbstractTaskAction _next = null;

    public void setNext(AbstractTaskAction p_next) {
        this._next = p_next;
    }

    public AbstractTaskAction next() {
        return this._next;
    }

    public abstract void eval(TaskContext context);

}
