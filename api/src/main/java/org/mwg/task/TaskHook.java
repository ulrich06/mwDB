package org.mwg.task;

public interface TaskHook {

    void on(TaskAction previous, TaskAction next, TaskContext context);

}
