package org.mwg.task;

@FunctionalInterface
public interface TaskAction {
    void eval(TaskContext context);
}
