package org.mwg.core.task;

import org.mwg.Node;
import org.mwg.plugin.AbstractNode;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionAdd implements TaskAction {

    private final String _relationName;
    private final String _variableNameToAdd;

    ActionAdd(String relationName, String variableNameToAdd) {
        this._relationName = relationName;
        this._variableNameToAdd = variableNameToAdd;
    }

    @Override
    public void eval(TaskContext context) {
        final Object previousResult = context.result();
        final Object savedVar = context.variable(_variableNameToAdd);
        if (savedVar instanceof AbstractNode) {
            if (previousResult instanceof AbstractNode) {
                ((Node) previousResult).add(_relationName, (Node) savedVar);
            } else if (previousResult instanceof Object[]) {
                addFromArray((Object[]) previousResult, _relationName, (Node) savedVar);
            }
        }
        context.setResult(previousResult);
        context.next();
    }

    private void addFromArray(final Object[] objs, final String relName, final Node toRemove) {
        for (int i = 0; i < objs.length; i++) {
            if (objs[i] instanceof AbstractNode) {
                ((AbstractNode) objs[i]).add(relName, toRemove);
            } else if (objs[i] instanceof Object[]) {
                addFromArray((Object[]) objs[i], relName, toRemove);
            }
        }
    }
}
