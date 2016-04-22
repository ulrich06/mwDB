package org.mwdb.task.action;

import org.mwdb.KCallback;
import org.mwdb.KNode;
import org.mwdb.KTaskAction;
import org.mwdb.KTaskContext;

public class ActionFromIndex implements KTaskAction {

    private final String _indexName;

    private final String _query;

    public ActionFromIndex(final String p_indexName, final String p_query) {
        _indexName = p_indexName;
        _query = p_query;
    }

    @Override
    public void eval(final KTaskContext context) {
        context.graph().find(context.getWorld(), context.getTime(), _indexName, _query, new KCallback<KNode[]>() {
            @Override
            public void on(KNode[] result) {
                context.setResult(result);
                context.next();
            }
        });
    }

}
