package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.DeferCounter;
import org.mwg.Node;
import org.mwg.core.utility.CoreDeferCounter;
import org.mwg.plugin.AbstractNode;
import org.mwg.plugin.Job;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

class ActionIndexOrUnindexNode implements TaskAction {
    private final String _indexName;
    private final String _flatKeyAttributes;
    private final boolean _isIndexation;

    ActionIndexOrUnindexNode(String indexName, String flatKeyAttributes, boolean isIndexation) {
        this._indexName = indexName;
        this._flatKeyAttributes = flatKeyAttributes;
        this._isIndexation = isIndexation;
    }

    @Override
    public void eval(TaskContext context) {

        /*
        final TaskResult previousResult = context.result();
        String templatedIndexName = context.template(_indexName);
        String templatedKeyAttributes = context.template(_flatKeyAttributes);

//        Node[] nodes = getNodes(previousResult);
        Node[] nodes = TaskHelper.flatNodes(previousResult, true);
        DeferCounter counter = new CoreDeferCounter(nodes.length);

        Callback<Boolean> end = new Callback<Boolean>() {
            @Override
            public void on(Boolean succeed) {
                if (succeed) {
                    counter.count();
                } else {
                    throw new RuntimeException("Error during indexation of node with id " + ((AbstractNode) previousResult).id());
                }
            }
        };

        for (int i = 0; i < nodes.length; i++) {
            if (_isIndexation) {
                context.graph().index(templatedIndexName, nodes[i], templatedKeyAttributes, end);
            } else {
                context.graph().unindex(templatedIndexName, nodes[i], templatedKeyAttributes, end);
            }
        }

        counter.then(new Job() {
            @Override
            public void run() {
                context.setRawResult(previousResult);
            }
        });
        */
    }

   /* private Node[] getNodes(Object previousResult) {
        if(previousResult instanceof AbstractNode) {
            return new Node[]{(Node) previousResult};
        }

        if(previousResult instanceof Object[]) {
            Object[] resAsArray = (Object[]) previousResult;
            Node[] nodes = new Node[0];
            for(int i=0;i<resAsArray.length;i++) {
                if(resAsArray[i] instanceof AbstractNode) {
                    Node tmp[] = new Node[nodes.length + 1];
                    System.arraycopy(nodes,0,tmp,0,nodes.length);
                    tmp[nodes.length] = (AbstractNode) resAsArray[i];
                    nodes = tmp;
                } else if(resAsArray[i] instanceof Object[]) {
                    Node[] innerNodes = getNodes(resAsArray[i]);
                    Node[] tmp = new Node[nodes.length + innerNodes.length];
                    System.arraycopy(nodes,0,tmp,0,nodes.length);
                    System.arraycopy(innerNodes,0,tmp,nodes.length,innerNodes.length);
                    nodes = tmp;
                } else {
                    throw new RuntimeException("[ActionIndexOrUnindexNode] The array in result contains an element with wrong type. " +
                            "Expected type: AbstractNode. Actual type: " + resAsArray[i]);
                }
            }
            return nodes;
        }

        throw new RuntimeException("[ActionIndexOrUnindexNode] Wrong type of result. Expected type is AbstractNode or an array of AbstractNode." +
                "Actual type is " + previousResult);
    }*/

    @Override
    public String toString() {
        if (_isIndexation) {
            return "index('" + _indexName + "','" + _flatKeyAttributes + "')";
        } else {
            return "unindex('" + _indexName + "','" + _flatKeyAttributes + "')";
        }
    }
}
