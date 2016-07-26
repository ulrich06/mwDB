package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.Constants;
import org.mwg.Node;
import org.mwg.plugin.AbstractTaskAction;
import org.mwg.task.TaskContext;

class ActionLookup extends AbstractTaskAction {

    private final String _world;
    private final String _time;
    private final String _id;

    ActionLookup(final String p_world, final String p_time, final String p_id) {
        super();
        this._world = p_world;
        this._time = p_time;
        this._id = p_id;
    }

    @Override
    public void eval(final TaskContext context) {
        final long worldL = Long.parseLong(context.template(_world));
        final long timeL = Long.parseLong(context.template(_time));
        final long idL = Long.parseLong(context.template(_id));
        context.graph().lookup(worldL, timeL, idL, new Callback<Node>() {
            @Override
            public void on(Node result) {
                context.continueWith(context.wrap(result));
            }
        });
    }

    @Override
    public String toString() {
        return "lookup(\'" + _world + "\'" + Constants.QUERY_SEP + "\'" + _time + "\'" + Constants.QUERY_SEP + "\'" + _id + "\")";
    }

}
