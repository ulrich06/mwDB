package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.Node;
import org.mwg.Query;
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
        Object previousResult = context.result();
        if (previousResult != null) {
            Node[] toLoad;
            if (previousResult instanceof AbstractNode) {
                toLoad = new Node[]{(Node) previousResult};
            } else if (previousResult instanceof Object[]) {
                toLoad = getNodes((Object[]) previousResult);
            } else {
                toLoad = new Node[0];
            }
            int countNbNodeToLoad = countNbNodeToLoad(toLoad);
            final CoreDeferCounter counter = new CoreDeferCounter(toLoad.length);
            final Node[] resultNodes = new AbstractNode[countNbNodeToLoad];
            final AtomicInteger cursor = new AtomicInteger(0);
            for (int i = 0; i < toLoad.length; i++) {
                Node node = toLoad[i];
                if (_query != null) {
                    Query queryObj = node.graph().newQuery();
                    queryObj.setWorld(context.world());
                    queryObj.setTime(context.time());
                    queryObj.parse(_query);
                    queryObj.setIndexName(_indexName);
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
                    node.findAll(_indexName, new Callback<Node[]>() {
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
                        context.setResult(resultNodes);
                    } else {
                        Node[] newResult = new Node[cursor.get()];
                        System.arraycopy(resultNodes, 0, newResult, 0, cursor.get());
                        context.setResult(newResult);
                    }
                    context.next();
                }
            });

        }
    }

    private Node[] getNodes(Object[] previousResult) {
        Node[] result = new Node[0];
        for (int i = 0; i < previousResult.length; i++) {
            if (previousResult[i] instanceof AbstractNode) {
                Node[] tmp = new Node[result.length + 1];
                System.arraycopy(result, 0, tmp, 0, result.length);
                tmp[result.length] = (Node) previousResult[i];
                result = tmp;
            } else if (previousResult[i] instanceof Object[]) {
                Node[] nodes = getNodes((Object[]) previousResult[i]);
                Node[] tmp = new Node[result.length + nodes.length];
                System.arraycopy(result, 0, tmp, 0, result.length);
                System.arraycopy(nodes, 0, tmp, result.length, nodes.length);
                result = tmp;
            }
        }
        return result;
    }

    private int countNbNodeToLoad(Node[] nodes) {
        int nbNoadToLoad = 0;
        for (Node node : nodes) {
            if (node != null) {
                LongLongArrayMap indexed = (LongLongArrayMap) node.get(_indexName);
                if (indexed != null) {//no index value
                    nbNoadToLoad += indexed.size();
                }
            }
        }
        return nbNoadToLoad;
    }
}
