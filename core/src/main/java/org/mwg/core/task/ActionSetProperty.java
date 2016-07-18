package org.mwg.core.task;

import org.mwg.Constants;
import org.mwg.Node;
import org.mwg.Type;
import org.mwg.plugin.AbstractNode;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

class ActionSetProperty implements TaskAction {

    private final String _relationName;
    private final String _variableNameToSet;
    private final byte _propertyType;

    ActionSetProperty(final String relationName, final byte propertyType, final String variableNameToSet) {
        this._relationName = relationName;
        this._variableNameToSet = variableNameToSet;
        this._propertyType = propertyType;
    }

    @Override
    public void eval(TaskContext context) {
        final TaskResult previousResult = context.result();
        final String flatRelationName = context.template(_relationName);
        if (previousResult != null) {
            Object toSet;
            Object templateBased = context.template(this._variableNameToSet);
            switch (_propertyType) {
                case Type.INT:
                    toSet = TaskHelper.parseInt(templateBased.toString());
                    break;
                case Type.DOUBLE:
                    toSet = Double.parseDouble(templateBased.toString());
                    break;
                case Type.LONG:
                    toSet = Long.parseLong(templateBased.toString());
                    break;
                default:
                    toSet = templateBased;
            }
            for (int i = 0; i < previousResult.size(); i++) {
                Object loopObj = previousResult.get(i);
                if (loopObj instanceof AbstractNode) {
                    Node loopNode = (Node) loopObj;
                    loopNode.setProperty(flatRelationName, _propertyType, toSet);
                }
            }
        }
        context.continueTask();
    }

    @Override
    public String toString() {
        return "setProperty(\'" + _relationName + "\'" + Constants.QUERY_SEP + "\'" + _propertyType + "\'" + Constants.QUERY_SEP + "\'" + _variableNameToSet + "\')";
    }

}
