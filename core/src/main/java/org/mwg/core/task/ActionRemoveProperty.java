package org.mwg.core.task;

import org.mwg.Node;
import org.mwg.plugin.AbstractNode;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionRemoveProperty implements TaskAction {

    private final String _propertyName;

    ActionRemoveProperty(String propertyName) {
        this._propertyName = propertyName;
    }

    @Override
    public void eval(TaskContext context) {
        Object previousResult = context.result();
        if (previousResult instanceof AbstractNode) {
            ((Node) previousResult).removeProperty(context.template(_propertyName));
        } else if (previousResult instanceof Object[]) {
            removePropertyFromArray((Object[]) previousResult,context.template(_propertyName));
        }
        context.setResult(previousResult);
    }

    private void removePropertyFromArray(Object[] objs, String templatedName) {
        for (int i = 0; i < objs.length; i++) {
            if (objs[i] instanceof AbstractNode) {
                ((AbstractNode) objs[i]).removeProperty(templatedName);
            } else if (objs[i] instanceof Object[]) {
                removePropertyFromArray((Object[]) objs[i],templatedName);
            }
        }
    }

    @Override
    public String toString() {
        return "removeProperty(\'" + _propertyName + "\')";
    }


}
