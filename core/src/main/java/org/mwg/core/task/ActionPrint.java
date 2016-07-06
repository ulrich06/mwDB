package org.mwg.core.task;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionPrint implements TaskAction {

    private String _name;

    ActionPrint(String p_name) {
        this._name = p_name;
    }

    @Override
    public void eval(final TaskContext context) {

        if(context.isVerbose()){
            for(int i=0;i<context.ident()+1;i++){
                System.out.print("\t");
            }
            System.out.println(context.template(_name));
        } else {
            System.out.println(context.template(_name));
        }
        final Object result = context.result();
        context.setUnsafeResult(result);
    }

    @Override
    public String toString() {
        return "print(\'" + _name + "\')";
    }

}
