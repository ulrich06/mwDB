package org.mwg.core.task;

import org.mwg.Node;
import org.mwg.plugin.AbstractNode;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionSet implements TaskAction {

    private final String _relationName;
    private final String _variableNameToSet;

    ActionSet(String relationName, String variableNameToSet) {
        this._relationName = relationName;
        this._variableNameToSet = variableNameToSet;
    }

    @Override
    public void eval(TaskContext context) {
        final Object previousResult = context.result();
        final Object savedVar = context.variable(_variableNameToSet);
        if (previousResult instanceof AbstractNode) {
            ((Node) previousResult).set(_relationName, savedVar);
        } else if (previousResult instanceof Object[]) {
            setFromArray((Object[]) previousResult, _relationName, savedVar);
        }
        context.setResult(previousResult);
        context.next();
    }

    private void setFromArray(final Object[] objs, final String relName, final Object toSet) {
        for (int i = 0; i < objs.length; i++) {
            if (objs[i] instanceof AbstractNode) {
                ((AbstractNode) objs[i]).set(relName, toSet);
            } else if (objs[i] instanceof Object[]) {
                setFromArray((Object[]) objs[i], relName, toSet);
            }
        }
    }

}
