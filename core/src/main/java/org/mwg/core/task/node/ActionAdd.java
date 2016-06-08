package org.mwg.core.task.node;

import org.mwg.Node;
import org.mwg.plugin.AbstractNode;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

public class ActionAdd implements TaskAction {
    private Node _relatedNode;
    private String _relationName;

    public ActionAdd(String relationName, Node relatedNode) {
        this._relatedNode = relatedNode;
        this._relationName = relationName;
    }

    @Override
    public void eval(TaskContext context) {
        Object previousResult = context.getPreviousResult();
        if(previousResult instanceof AbstractNode) {
            ((AbstractNode)previousResult).add(_relationName,_relatedNode);

        } else if(previousResult instanceof Object[]) {
            Object[] array = (Object[]) previousResult;
            if(array.length > 0 && array[0] instanceof AbstractNode) {
                for(int i=0;i<array.length;i++) {
                    ((AbstractNode)array[i]).add(_relationName,_relatedNode);
                }
            } else if(array.length > 0) {
                throw new RuntimeException("The previous result in the context has an incompatible type with nodeSet action: "
                        + previousResult.getClass().getName());
            }

        } else if(previousResult != null) {
            throw new RuntimeException("The previous result in the context has an incompatible type with nodeSet action: "
                    + previousResult.getClass().getName());
        }


        context.setResult(previousResult);
        context.next();
    }
}
