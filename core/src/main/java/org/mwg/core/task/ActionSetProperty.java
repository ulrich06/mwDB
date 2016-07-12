package org.mwg.core.task;

import org.mwg.Constants;
import org.mwg.Node;
import org.mwg.Type;
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
        String flatRelationName = context.template(_relationName);

        Object savedVar = context.variable(_variableNameToSet);
        if (savedVar == null) {
            Object templateBased = context.template(this._variableNameToSet);
            switch (_propertyType) {
                case Type.INT:
                    savedVar = parseInt(templateBased.toString());
                    break;
                case Type.DOUBLE:
                    savedVar = Double.parseDouble(templateBased.toString());
                    break;
                case Type.LONG:
                    savedVar = Long.parseLong(templateBased.toString());
                    break;
                default:
                    savedVar = templateBased;
            }
        }

        if (previousResult instanceof AbstractNode) {
            ((Node) previousResult).setProperty(flatRelationName, _propertyType, savedVar);
        } else if (previousResult instanceof Object[]) {
            setFromArray((Object[]) previousResult, flatRelationName, savedVar);
        }
        context.setUnsafeResult(previousResult);
    }

    /**
     * @native ts
     * return parseInt(payload);
     */
    private int parseInt(String payload) {
        return Integer.parseInt(payload);
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

    @Override
    public String toString() {
        return "setProperty(\'" + _relationName + "\'" + Constants.QUERY_SEP + "\'" + _propertyType + "\'" + Constants.QUERY_SEP + "\'" + _variableNameToSet + "\')";
    }

}
