package org.mwdb.task.action;

import org.mwdb.KCallback;
import org.mwdb.KNode;
import org.mwdb.KTaskAction;
import org.mwdb.KTaskContext;

public class ActionFromIndexAll implements KTaskAction {

    private final String _indexName;

    public ActionFromIndexAll(final String p_indexName) {
        _indexName = p_indexName;
    }

    @Override
    public void eval(final KTaskContext context) {
        context.graph().all(context.getWorld(), context.getTime(), _indexName, new KCallback<KNode[]>() {
            @Override
            public void on(KNode[] result) {
                context.setResult(result);
                context.next();
            }
        });
    }

}
