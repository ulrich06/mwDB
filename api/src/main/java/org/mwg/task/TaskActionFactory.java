package org.mwg.task;

@FunctionalInterface
public interface TaskActionFactory {

    TaskAction create(String[] params);

}
