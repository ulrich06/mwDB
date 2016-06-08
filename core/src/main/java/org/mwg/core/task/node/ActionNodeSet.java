package org.mwg.core.task.node;

import org.mwg.plugin.AbstractNode;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

/**
 * Created by ludovicmouline on 08/06/16.
 */
public class ActionNodeSet implements TaskAction {
    private String _propertyName;
    private Object _propertyValue;

    public ActionNodeSet(String propertyName, Object propertyValue) {
        this._propertyName = propertyName;
        this._propertyValue = propertyValue;
    }

    @Override
    public void eval(TaskContext context) {
        Object previousResult = context.getPreviousResult();
        if(previousResult instanceof AbstractNode) {
            ((AbstractNode)previousResult).set(_propertyName,_propertyValue);

        } else if(previousResult instanceof Object[]) {
            Object[] array = (Object[]) previousResult;
            if(array.length > 0 && array[0] instanceof AbstractNode) {
                for(int i=0;i<array.length;i++) {
                    ((AbstractNode)array[i]).set(_propertyName,_propertyValue);
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
