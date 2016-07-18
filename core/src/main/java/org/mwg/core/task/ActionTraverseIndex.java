package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.DeferCounter;
import org.mwg.Node;
import org.mwg.Query;
import org.mwg.core.CoreConstants;
import org.mwg.core.utility.CoreDeferCounter;
import org.mwg.plugin.AbstractNode;
import org.mwg.plugin.Job;
import org.mwg.struct.LongLongArrayMap;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

import java.util.concurrent.atomic.AtomicInteger;

class ActionTraverseIndex implements TaskAction {
    private String _indexName;
    private String _query;

    ActionTraverseIndex(String indexName, String query) {
        this._query = query;
        this._indexName = indexName;
    }

    @Override
    public void eval(final TaskContext context) {
        final TaskResult finalResult = context.wrap(null);
        final String flatName = context.template(_indexName);
        final TaskResult previousResult = context.result();
        if (previousResult != null) {
            final int previousSize = previousResult.size();
            final DeferCounter defer = context.graph().newCounter(previousSize);
            for (int i = 0; i < previousSize; i++) {
                final Object loop = previousResult.get(i);
                if (loop instanceof AbstractNode) {
                    Node casted = (Node) loop;
                    casted.find(flatName, _query, new Callback<Node[]>() {
                        @Override
                        public void on(Node[] result) {
                            if (result != null) {
                                for (int j = 0; j < result.length; j++) {
                                    if (result[j] != null) {
                                        finalResult.add(result[j]);
                                    }
                                }
                            }
                            defer.count();
                        }
                    });
                } else {
                    //TODO add closable management
                    finalResult.add(loop);
                    defer.count();
                }
            }
            defer.then(new Job() {
                @Override
                public void run() {
                    context.continueWith(finalResult);
                }
            });
        } else {
            context.continueTask();
        }
    }

    @Override
    public String toString() {
        return "traverseIndex(\'" + _indexName + CoreConstants.QUERY_SEP + _query + "\')";
    }

}

