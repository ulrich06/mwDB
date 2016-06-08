package org.mwg.core.task.node;

import org.mwg.Node;
import org.mwg.core.CoreConstants;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;


/**
 * Created by ludovicmouline on 08/06/16.
 */
public class ActionCreateNode implements TaskAction {
    private long _world;
    private long _time;


    public ActionCreateNode(long world, long time) {
        this._world = world;
        this._time = time;
    }

    @Override
    public void eval(TaskContext context) {
        if(_world == CoreConstants.NULL_LONG && _time == CoreConstants.NULL_LONG) {
            _world = context.getWorld();
            _time = context.getTime();
        }
        Node newNode = context.graph().newNode(_world,_time);
        context.setResult(newNode);
        context.next();
    }
}
