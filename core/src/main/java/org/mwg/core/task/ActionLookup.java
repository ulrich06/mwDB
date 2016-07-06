package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.Constants;
import org.mwg.Node;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionLookup implements TaskAction {

    private final String _world;
    private final String _time;
    private final String _id;

    public ActionLookup(String p_world, String p_time, String p_id) {
        this._world = p_world;
        this._time = p_time;
        this._id = p_id;
    }

    @Override
    public void eval(TaskContext context) {

        String parsedWorldResult = context.template(_world);
        long worldL = parse(parsedWorldResult);

        String parseTimeResult = context.template(_time);
        long timeL = parse(parseTimeResult);

        String parsedId = context.template(_id);
        long idL = parse(parsedId);

        Object previous = context.result();
        context.cleanObj(previous);

        context.graph().lookup(worldL, timeL, idL, new Callback<Node>() {
            @Override
            public void on(Node result) {
                context.setUnsafeResult(result);
            }
        });

    }

    /**
     * @native ts
     * return parseInt(flat);
     */
    private long parse(String flat) {
        return Long.parseLong(flat);
    }

    @Override
    public String toString() {
        return "lookup(" + _world + Constants.QUERY_SEP + _time + Constants.QUERY_SEP + _id + ")";
    }

}
