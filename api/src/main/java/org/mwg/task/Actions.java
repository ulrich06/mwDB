package org.mwg.task;

import org.mwg.Graph;
import org.mwg.GraphBuilder;

public class Actions {

    private static GraphBuilder.InternalBuilder _internalBuilder = null;

    /**
     * To call oce all options have been set, to actually create a task instance.
     *
     * @return the {@link Graph}
     * @native ts
     * if (org.mwg.task.Actions._internalBuilder == null) {
     * org.mwg.task.Actions._internalBuilder = new org.mwg.core.Builder();
     * }
     * return org.mwg.task.Actions._internalBuilder.newTask();
     */
    private static Task newTask() {
        if (_internalBuilder == null) {
            try {
                _internalBuilder = (GraphBuilder.InternalBuilder) Actions.class.getClassLoader().loadClass("org.mwg.core.Builder").newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return _internalBuilder.newTask();
    }

    public static Task setWorld(long world) {
        return newTask().setWorld(world);
    }

    public static Task setTime(long world) {
        return newTask().setTime(world);
    }

    public static Task fromVar(String variableName) {
        return newTask().fromVar(variableName);
    }

    public static Task then(Action action) {
        return newTask().then(action);
    }

    public static Task from(Object input) {
        return newTask().from(input);
    }

    public static Task fromIndexAll(String indexName) {
        return newTask().fromIndexAll(indexName);
    }

    public static Task fromIndex(String indexName, String query) {
        return newTask().fromIndex(indexName, query);
    }

    public static Task parse(String flatTask) {
        return newTask().parse(flatTask);
    }

}
