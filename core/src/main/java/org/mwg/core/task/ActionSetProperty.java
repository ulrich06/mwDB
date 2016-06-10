package org.mwg.core.task;

import org.mwg.Node;
import org.mwg.plugin.AbstractNode;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionSetProperty implements TaskAction {

    private final String _relationName;
    private final String _variableNameToSet;
    private final byte _propertyType;

    ActionSetProperty(String relationName, byte propertyType, String variableNameToSet) {
        this._relationName = relationName;
        this._variableNameToSet = variableNameToSet;
        this._propertyType = propertyType;
    }

    @Override
    public void eval(TaskContext context) {
        final Object previousResult = context.result();
        final Object savedVar = context.variable(_variableNameToSet);
        if (previousResult instanceof AbstractNode) {
            ((Node) previousResult).setProperty(_relationName, _propertyType, savedVar);
        } else if (previousResult instanceof Object[]) {
            setFromArray((Object[]) previousResult, _relationName, savedVar);
        }
        context.setResult(previousResult);
        context.next();
    }

    private void setFromArray(final Object[] objs, final String relName, final Object toSet) {
        for (int i = 0; i < objs.length; i++) {
            if (objs[i] instanceof AbstractNode) {
                ((AbstractNode) objs[i]).setProperty(relName, _propertyType, toSet);
            } else if (objs[i] instanceof Object[]) {
                setFromArray((Object[]) objs[i], relName, toSet);
            }
        }
    }

}
