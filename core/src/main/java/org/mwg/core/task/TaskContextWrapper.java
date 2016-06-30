package org.mwg.core.task;

import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.task.TaskContext;

/**
 * A wrapper of a CoreTaskContext
 * Protect the call to the next method, that means that we cannot call it on an instance of this class
 */
class TaskContextWrapper implements TaskContext {

    private final TaskContext _wrapped;

    TaskContextWrapper(TaskContext p_wrapped) {
        _wrapped = p_wrapped;
    }

    @Override
    public Graph graph() {
        return _wrapped.graph();
    }

    @Override
    public long world() {
        return _wrapped.world();
    }

    @Override
    public void setWorld(long world) {
        _wrapped.setWorld(world);
    }

    @Override
    public long time() {
        return _wrapped.time();
    }

    @Override
    public void setTime(long time) {
        _wrapped.setTime(time);
    }

    @Override
    public Object variable(String name) {
        return _wrapped.variable(name);
    }

    @Override
    public String[] variablesKeys() {
        return _wrapped.variablesKeys();
    }

    @Override
    public void setVariable(String name, Object value) {
        _wrapped.setVariable(name, value);
    }

    @Override
    public void addToVariable(String name, Object value) {
        _wrapped.addToVariable(name, value);
    }

    @Override
    public Object result() {
        return _wrapped.result();
    }

    @Override
    public Object[] resultAsObjectArray() {
        return _wrapped.resultAsObjectArray();
    }

    @Override
    public String resultAsString() {
        return _wrapped.resultAsString();
    }

    @Override
    public String[] resultAsStringArray() {
        return _wrapped.resultAsStringArray();
    }

    @Override
    public Node resultAsNode() {
        return _wrapped.resultAsNode();
    }

    @Override
    public Node[] resultAsNodeArray() {
        return _wrapped.resultAsNodeArray();
    }

    @Override
    public void setResult(Object actionResult) {
        _wrapped.setResult(actionResult);
    }

    @Override
    public void next() {
        //noop action here, because the next action will be automatically called
    }

    @Override
    public void clean() {
        _wrapped.clean();
    }

    @Override
    public String toString() {
        return _wrapped.toString();
    }
}
