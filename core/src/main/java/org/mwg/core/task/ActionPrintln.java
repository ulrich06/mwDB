package org.mwg.core.task;

import org.mwg.Node;
import org.mwg.plugin.AbstractNode;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionPrintln implements TaskAction {

    @Override
    public void eval(final TaskContext context) {
        Object result = context.result();
        if (result instanceof AbstractNode[]) {
            Node[] casted = (Node[]) result;
            System.out.print("[");
            for (int i = 0; i < casted.length; i++) {
                if (i != 0) {
                    System.out.print(",");
                }
                System.out.print(casted[i].toString());
            }
            System.out.println("]");
        } else {
            System.out.println(result);
        }
        context.setResult(result);
        context.next();
    }

}
