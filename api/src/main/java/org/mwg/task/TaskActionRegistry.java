package org.mwg.task;

public interface TaskActionRegistry {

    void add(String name, TaskActionFactory action);

    void remove(String name);

    TaskActionFactory get(String name);

}
