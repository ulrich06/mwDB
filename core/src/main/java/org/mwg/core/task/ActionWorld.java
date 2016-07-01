package org.mwg.core.task;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionWorld implements TaskAction {

    private final long _world;

    ActionWorld(final long p_world) {
        this._world = p_world;
    }

    @Override
    public void eval(final TaskContext context) {
        context.setWorld(_world);
        context.setResult(context.result());
    }

}
