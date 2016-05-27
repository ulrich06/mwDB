package org.mwg.task;

@FunctionalInterface
public interface Action {
    void eval(TaskContext context);
}
