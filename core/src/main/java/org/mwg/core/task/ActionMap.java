package org.mwg.core.task;

import org.mwg.Node;
import org.mwg.plugin.AbstractNode;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskFunctionMap;
import org.mwg.task.TaskResult;

class ActionMap implements TaskAction {

    private final TaskFunctionMap _map;

    ActionMap(final TaskFunctionMap p_map) {
        this._map = p_map;
    }

    @Override
    public final void eval(final TaskContext context) {
        final TaskResult previous = context.result();
        final TaskResult next = context.wrap(null);
        final int previousSize = previous.size();
        for (int i = 0; i < previousSize; i++) {
            final Object loop = previous.get(i);
            if (loop instanceof AbstractNode) {
                next.add(_map.map((Node) loop));
            } else {
                next.add(loop);
            }
        }
        context.continueWith(next);
    }

    @Override
    public String toString() {
        return "map()";
    }

}
