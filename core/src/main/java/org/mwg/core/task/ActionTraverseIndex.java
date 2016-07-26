package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.DeferCounter;
import org.mwg.Node;
import org.mwg.core.CoreConstants;
import org.mwg.plugin.AbstractNode;
import org.mwg.plugin.AbstractTaskAction;
import org.mwg.plugin.Job;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

class ActionTraverseIndex extends AbstractTaskAction {
    private String _indexName;
    private String _query;

    ActionTraverseIndex(final String indexName, final String query) {
        super();
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
                    final Node casted = (Node) loop;
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
                            casted.free();
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
                    //optimization to avoid iteration on previous result for free
                    previousResult.clear();
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

