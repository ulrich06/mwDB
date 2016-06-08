package org.mwg.core.task;

import org.mwg.Node;
import org.mwg.plugin.AbstractNode;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionRemove implements TaskAction {

    private final String _relationName;
    private final String _variableNameToRemove;

    ActionRemove(String relationName, String variableNameToRemove) {
        this._relationName = relationName;
        this._variableNameToRemove = variableNameToRemove;
    }

    @Override
    public void eval(TaskContext context) {
        final Object previousResult = context.getPreviousResult();
        final Object savedVar = context.getVariable(_variableNameToRemove);
        if (savedVar instanceof AbstractNode) {
            if (previousResult instanceof AbstractNode) {
                ((Node) previousResult).remove(_relationName, (Node) savedVar);
            } else if (previousResult instanceof Object[]) {
                removeFromArray((Object[]) previousResult, _relationName, (Node) savedVar);
            }
        }
        context.setResult(previousResult);
        context.next();
    }

    private void removeFromArray(final Object[] objs, final String relName, final Node toRemove) {
        for (int i = 0; i < objs.length; i++) {
            if (objs[i] instanceof AbstractNode) {
                ((AbstractNode) objs[i]).remove(relName, toRemove);
            } else if (objs[i] instanceof Object[]) {
                removeFromArray((Object[]) objs[i], relName, toRemove);
            }
        }
    }

}
