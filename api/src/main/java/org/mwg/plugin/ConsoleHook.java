package org.mwg.plugin;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskHook;

public class ConsoleHook implements TaskHook {

    private static ConsoleHook _instance = null;

    public static ConsoleHook instance() {
        if (_instance == null) {
            _instance = new ConsoleHook();
        }
        return _instance;
    }

    @Override
    public void on(TaskAction previous, TaskAction next, TaskContext context) {
        for (int i = 0; i < context.ident(); i++) {
            System.out.print("\t");
        }
        String taskName = next.toString();
        System.out.println(context.template(taskName));
        /*
        for (int i = 0; i < context.ident(); i++) {
            System.out.print("\t");
            System.out.println(context.result().toString());
        }*/

    }
}
