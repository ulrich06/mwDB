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
    public static Task newTask() {
        if (_internalBuilder == null) {
            try {
                _internalBuilder = (GraphBuilder.InternalBuilder) Actions.class.getClassLoader().loadClass("org.mwg.core.Builder").newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return _internalBuilder.newTask();
    }

    public static Task setWorld(String variableName) {
        return newTask().setWorld(variableName);
    }

    public static Task setTime(String variableName) {
        return newTask().setTime(variableName);
    }

    public static Task then(Action action) {
        return newTask().then(action);
    }

    public static Task inject(Object input) {
        return newTask().inject(input);
    }

    public static Task fromVar(String variableName) {
        return newTask().fromVar(variableName);
    }

    public static Task fromVarAt(String variableName, int index) {
        return newTask().fromVarAt(variableName, index);
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

    public static Task asVar(String variableName) {
        return newTask().asVar(variableName);
    }

    public static Task setLocalVar(String variableName, Object inputValue) {
        return newTask().setLocalVar(variableName, inputValue);
    }

    public static Task setGlobalVar(String variableName, Object inputValue) {
        return newTask().setLocalVar(variableName, inputValue);
    }

    public static Task map(TaskFunctionMap mapFunction) {
        return newTask().map(mapFunction);
    }

    public static Task selectWith(String name, String pattern) {
        return newTask().selectWith(name, pattern);
    }

    public static Task selectWithout(String name, String pattern) {
        return newTask().selectWithout(name, pattern);
    }

    public static Task select(TaskFunctionSelect filterFunction) {
        return newTask().select(filterFunction);
    }

    public static Task selectObject(TaskFunctionSelectObject filterFunction) {
        return newTask().selectObject(filterFunction);
    }

    public static Task traverse(String relationName) {
        return newTask().traverse(relationName);
    }

    public static Task get(String name) {
        return newTask().get(name);
    }

    public static Task traverseIndex(String indexName, String query) {
        return newTask().traverseIndex(indexName, query);
    }

    public static Task traverseOrKeep(String relationName) {
        return newTask().traverseOrKeep(relationName);
    }

    public static Task traverseIndexAll(String indexName) {
        return newTask().traverseIndexAll(indexName);
    }

    public static Task repeat(String repetition, Task subTask) {
        return newTask().repeat(repetition, subTask);
    }

    public static Task repeatPar(String repetition, Task subTask) {
        return newTask().repeatPar(repetition, subTask);
    }

    public static Task print(String name) {
        return newTask().print(name);
    }

    public static Task setProperty(String propertyName, byte propertyType, String variableNameToSet) {
        return newTask().setProperty(propertyName, propertyType, variableNameToSet);
    }

    public static Task whileDo(TaskFunctionConditional condition, Task task) {
        return newTask().whileDo(condition, task);
    }

    public static Task doWhile(Task task, TaskFunctionConditional condition) {
        return newTask().doWhile(task, condition);
    }

    public static Task selectWhere(Task subTask) {
        return newTask().selectWhere(subTask);
    }

    public static Task foreach(Task subTask) {
        return newTask().foreach(subTask);
    }

    public static Task foreachPar(Task subTask) {
        return newTask().foreachPar(subTask);
    }

    public static Task math(String expression) {
        return newTask().math(expression);
    }

    public static Task action(String name, String params) {
        return newTask().action(name, params);
    }

    public static Task remove(String relationName, String variableNameToRemove) {
        return newTask().remove(relationName, variableNameToRemove);
    }

    public static Task add(String relationName, String variableNameToAdd) {
        return newTask().add(relationName, variableNameToAdd);
    }

    public static Task jump(String time) {
        return newTask().jump(time);
    }

    public static Task removeProperty(String propertyName) {
        return newTask().removeProperty(propertyName);
    }

    public static Task newNode() {
        return newTask().newNode();
    }

    public static Task newTypedNode(String nodeType) {
        return newTask().newTypedNode(nodeType);
    }

    public static Task save() {
        return newTask().save();
    }

    public static Task ifThen(TaskFunctionConditional cond, Task then) {
        return newTask().ifThen(cond, then);
    }

    public static Task split(String splitPattern) {
        return newTask().split(splitPattern);
    }

    public static Task lookup(String world, String time, String id) {
        return newTask().lookup(world, time, id);
    }

}
