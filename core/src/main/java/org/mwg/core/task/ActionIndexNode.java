package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.DeferCounter;
import org.mwg.Node;
import org.mwg.core.utility.CoreDeferCounter;
import org.mwg.plugin.AbstractNode;
import org.mwg.plugin.Job;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionIndexNode implements TaskAction {
    private final String _indexName;
    private final String _flatKeyAttributes;

    public ActionIndexNode(String indexName, String flatKeyAttributes) {
        this._indexName = indexName;
        this._flatKeyAttributes = flatKeyAttributes;
    }

    @Override
    public void eval(TaskContext context) {
        final Object previousResult = context.result();

        String templatedIndexName = context.template(_indexName);
        String templatedKeyAttributes = context.template(_flatKeyAttributes);

        Node[] nodes = getNodes(previousResult);
        DeferCounter counter = new CoreDeferCounter(nodes.length);
        for(int i = 0;i < nodes.length; i++) {
            context.graph().index(templatedIndexName, nodes[i], templatedKeyAttributes, new Callback<Boolean>() {
                @Override
                public void on(Boolean succeed) {
                    if(succeed) {
                       counter.count();
                    } else {
                        throw new RuntimeException("Error during indexation of node with id " + ((AbstractNode) previousResult).id());
                    }
                }
            });
        }
        counter.then(new Job() {
            @Override
            public void run() {
                context.setUnsafeResult(previousResult);
            }
        });
    }

    private Node[] getNodes(Object previousResult) {
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
                    throw new RuntimeException("[ActionIndexNode] The array in result contains an element with wrong type. " +
                            "Expected type: AbstractNode. Actual type: " + resAsArray[i].getClass());
                }
            }
            return nodes;
        }

        throw new RuntimeException("[ActionIndexNode] Wrong type of result. Expected type is AbstractNode or an array of AbstractNode." +
                "Actual type is " + previousResult.getClass());
    }
}
