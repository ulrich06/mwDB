package org.mwg.core.task;

import org.mwg.Node;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionNewNode implements TaskAction {

    private final String typeNode;


    public ActionNewNode(String typeNode) {
        this.typeNode = typeNode;
    }

    @Override
    public void eval(TaskContext context) {
            Object previous = context.result();

        Node newNode;
        if(typeNode == null) {
           newNode = context.graph().newNode(context.world(), context.time());
        } else {
            String templatedType = context.template(typeNode);
            context.cleanObj(previous);
            newNode = context.graph().newTypedNode(context.world(),context.time(),templatedType);
        }

        context.cleanObj(previous);
        context.setUnsafeResult(newNode);
    }

    @Override
    public String toString() {
        return "newNode()";
    }

}
