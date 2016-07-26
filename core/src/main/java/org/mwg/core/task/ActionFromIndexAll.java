package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.Node;
import org.mwg.plugin.AbstractTaskAction;
import org.mwg.task.TaskContext;

class ActionFromIndexAll extends AbstractTaskAction {

    private final String _indexName;

    ActionFromIndexAll(final String p_indexName) {
        super();
        _indexName = p_indexName;
    }

    @Override
    public void eval(final TaskContext context) {
        context.graph().findAll(context.world(), context.time(), _indexName, new Callback<Node[]>() {
            @Override
            public void on(Node[] result) {
                context.continueWith(context.wrap(result));
            }
        });
    }

    @Override
    public String toString() {
        return "fromIndexAll(\'" + _indexName + "\')";
    }

}
