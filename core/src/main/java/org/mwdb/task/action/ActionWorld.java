package org.mwdb.task.action;

import org.mwdb.KTaskAction;
import org.mwdb.KTaskContext;

public class ActionWorld implements KTaskAction {

    private final long _world;

    public ActionWorld(final long p_world) {
        this._world = p_world;
    }

    @Override
    public void eval(final KTaskContext context) {
        context.setWorld(_world);
        context.setResult(context.getPreviousResult());
        context.next();
    }

}
