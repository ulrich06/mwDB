package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.Node;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionFromIndexAll implements TaskAction {

    private final String _indexName;

    ActionFromIndexAll(final String p_indexName) {
        _indexName = p_indexName;
    }

    @Override
    public void eval(final TaskContext context) {
        context.graph().findAll(context.world(), context.time(), _indexName, new Callback<Node[]>() {
            @Override
            public void on(Node[] result) {
                context.setResult(result);
                context.next();
            }
        });
    }

}
