package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.Constants;
import org.mwg.Node;
import org.mwg.plugin.AbstractTaskAction;
import org.mwg.task.TaskContext;

class ActionFromIndex extends AbstractTaskAction {

    private final String _indexName;
    private final String _query;

    ActionFromIndex(final String p_indexName, final String p_query) {
        super();
        _indexName = p_indexName;
        _query = p_query;
    }

    @Override
    public void eval(final TaskContext context) {
        final String flatIndexName = context.template(_indexName);
        final String flatQuery = context.template(_query);
        context.graph().find(context.world(), context.time(), flatIndexName, flatQuery, new Callback<Node[]>() {
            @Override
            public void on(Node[] result) {
                context.continueWith(context.wrap(result));
            }
        });
    }

    @Override
    public String toString() {
        return "fromIndex(\'" + _indexName + "\'" + Constants.QUERY_SEP + "\'" + _query + "\')";
    }

}
