package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.Node;
import org.mwg.Query;
import org.mwg.core.CoreConstants;
import org.mwg.core.utility.CoreDeferCounter;
import org.mwg.plugin.AbstractNode;
import org.mwg.plugin.Job;
import org.mwg.struct.LongLongArrayMap;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

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
        /*
        //todo replace setResult by setUnsafeResult
        Object previousResult = context.result();
        if (previousResult != null) {
            String flatIndexName = context.template(_indexName);
            String flatQuery = context.template(_query);
            Node[] toLoad = TaskHelper.flatNodes(previousResult,false);
            int countNbNodeToLoad = countNbNodeToLoad(toLoad,flatIndexName);
            final CoreDeferCounter counter = new CoreDeferCounter(toLoad.length);
            final Node[] resultNodes = new AbstractNode[countNbNodeToLoad];
            final AtomicInteger cursor = new AtomicInteger(0);
            for (int i = 0; i < toLoad.length; i++) {
                Node node = toLoad[i];
                if (flatQuery != null) {
                    Query queryObj = node.graph().newQuery();
                    queryObj.setWorld(context.world());
                    queryObj.setTime(context.time());
                    queryObj.parse(flatQuery);
                    queryObj.setIndexName(flatIndexName);
                    node.findByQuery(queryObj, new Callback<Node[]>() {
                        @Override
                        public void on(Node[] result) {
                            for (Node n : result) {
                                if (n != null) {
                                    resultNodes[cursor.getAndIncrement()] = n;
                                }
                            }
                            counter.count();
                        }
                    });
                } else {
                    node.findAll(flatIndexName, new Callback<Node[]>() {
                        @Override
                        public void on(Node[] result) {
                            for (Node n : result) {
                                if (n != null) {
                                    resultNodes[cursor.getAndIncrement()] = n;
                                }
                            }
                            counter.count();
                        }
                    });
                }
            }
            counter.then(new Job() {
                @Override
                public void run() {
                    if (cursor.get() == resultNodes.length) {
                        //todo set with unsafe
                        context.setResult(resultNodes);
                    } else {
                        Node[] newResult = new Node[cursor.get()];
                        System.arraycopy(resultNodes, 0, newResult, 0, cursor.get());
                        //todo set with unsafe
                        context.setResult(newResult);
                    }
                }
            });

        } else {
            context.setRawResult(null);
        }

*/

    }

    @Override
    public String toString() {
        return "traverseIndex(\'" + _indexName + CoreConstants.QUERY_SEP + _query + "\')";
    }

}

