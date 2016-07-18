package org.mwg.core.task;

import org.mwg.Constants;
import org.mwg.Node;
import org.mwg.plugin.AbstractNode;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;
import org.mwg.task.TaskResultIterator;

class ActionRemove implements TaskAction {

    private final String _relationName;
    private final String _variableNameToRemove;

    ActionRemove(String relationName, String variableNameToRemove) {
        this._relationName = relationName;
        this._variableNameToRemove = variableNameToRemove;
    }

    @Override
    public void eval(final TaskContext context) {
        final TaskResult previousResult = context.result();
        final TaskResult savedVar = context.variable(context.template(_variableNameToRemove));
        if (previousResult != null && savedVar != null) {
            final String relName = context.template(_relationName);
            final TaskResultIterator previousResultIt = previousResult.iterator();
            Object iter = previousResultIt.next();
            while (iter != null) {
                if (iter instanceof AbstractNode) {
                    final TaskResultIterator savedVarIt = savedVar.iterator();
                    Object toRemoveIter = savedVarIt.next();
                    while (toRemoveIter != null) {
                        if (toRemoveIter instanceof AbstractNode) {
                            ((Node) iter).remove(relName, (Node) toRemoveIter);
                        }
                        toRemoveIter = savedVarIt.next();
                    }
                }
                iter = previousResultIt.next();
            }
        }
        context.continueTask();
    }

    @Override
    public String toString() {
        return "remove(\'" + _relationName + "\'" + Constants.QUERY_SEP + "\'" + _variableNameToRemove + "\')";
    }


}
