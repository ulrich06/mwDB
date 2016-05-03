package org.mwg.core.task;

import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.plugin.AbstractNode;
import org.mwg.task.TaskAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

class CoreTaskContext implements org.mwg.task.TaskContext {

    private final Map<String, Object> _variables;
    private final Object[] _results;
    private final Graph _graph;
    private final TaskAction[] _actions;
    private final AtomicInteger _currentTaskId;
    private final org.mwg.task.TaskContext _parentContext;
    private final Object _initialResult;

    private long _world;
    private long _time;

    CoreTaskContext(final org.mwg.task.TaskContext p_parentContext, final Object p_initialResult, final Graph p_graph, final TaskAction[] p_actions) {
        this._world = 0;
        this._time = 0;
        this._graph = p_graph;
        this._parentContext = p_parentContext;
        this._initialResult = p_initialResult;
        this._variables = new ConcurrentHashMap<String, Object>();
        this._results = new Object[p_actions.length];
        this._actions = p_actions;
        this._currentTaskId = new AtomicInteger(0);
    }

    @Override
    public final Graph graph() {
        return _graph;
    }

    @Override
    public final long getWorld() {
        return this._world;
    }

    @Override
    public final void setWorld(long p_world) {
        this._world = p_world;
    }

    @Override
    public final long getTime() {
        return this._time;
    }

    @Override
    public final void setTime(long p_time) {
        this._time = p_time;
    }

    @Override
    public final Object getVariable(String name) {
        Object result = this._variables.get(name);
        if (result != null) {
            return result;
        }
        if (_parentContext != null) {
            return this._parentContext.getVariable(name);
        }
        return null;
    }

    @Override
    public String[] getVariablesKeys() {
        String[] result = new String[this._variables.size()];
        int index = 0;
        for(String key : this._variables.keySet()) {
            result[index++] = key;
        }
        return result;
    }

    @Override
    public final void setVariable(String name, Object value) {
        if (value != null) {
            this._variables.put(name, value);
        } else {
            this._variables.remove(name);
        }
    }

    @Override
    public final Object getPreviousResult() {
        int current = _currentTaskId.get();
        if (current == 0) {
            return _initialResult;
        } else {
            Object previousResult = _results[current - 1];
            if (previousResult != null && previousResult instanceof org.mwg.task.TaskContext) {
                return ((org.mwg.task.TaskContext) previousResult).getPreviousResult();
            } else if (previousResult != null && previousResult instanceof org.mwg.core.task.CoreTaskContext[]) {
                org.mwg.core.task.CoreTaskContext[] contexts = (org.mwg.core.task.CoreTaskContext[]) previousResult;
                List<Object> result = new ArrayList<Object>();
                for (int i = 0; i < contexts.length; i++) {
                    Object currentLoop = contexts[i].getPreviousResult();
                    if (currentLoop != null) {
                        result.add(currentLoop);
                    }
                }
                return result.toArray(new Object[result.size()]);
            } else {
                return previousResult;
            }
        }
    }

    @Override
    public final void setResult(Object actionResult) {
        if(actionResult instanceof org.mwg.core.task.CoreTaskContext) {
            mergeVariables((org.mwg.task.TaskContext) actionResult);
        } else if(actionResult instanceof org.mwg.core.task.CoreTaskContext[]) {
            for(org.mwg.task.TaskContext taskContext : (org.mwg.core.task.CoreTaskContext[])actionResult) {
                mergeVariables(taskContext);
            }
        }
        this._results[_currentTaskId.get()] = actionResult;
    }

    private void mergeVariables(org.mwg.task.TaskContext actionResult) {
        String[]variables = actionResult.getVariablesKeys();
        for(String variableName : variables) {
            this.setVariable(variableName,actionResult.getVariable(variableName));
        }
    }

    @Override
    public final void next() {
        TaskAction nextAction = _actions[_currentTaskId.incrementAndGet()];
        nextAction.eval(this);
    }

    @Override
    public final void clean() {
        for (int i = 0; i < _results.length; i++) {
            cleanObj(_results[i]);
        }
    }

    private void cleanObj(Object o) {
        if (o instanceof AbstractNode) {
            ((Node) o).free();
        } else if (o instanceof org.mwg.core.task.CoreTaskContext) {
            ((org.mwg.task.TaskContext) o).clean();
        } else if (o instanceof org.mwg.core.task.CoreTaskContext[]) {
            org.mwg.core.task.CoreTaskContext[] loop = (org.mwg.core.task.CoreTaskContext[]) o;
            for (int j = 0; j < loop.length; j++) {
                loop[j].clean();
            }
        } else if (o instanceof Object[]) {
            Object[] loop = (Object[]) o;
            for (int j = 0; j < loop.length; j++) {
                cleanObj(loop[j]);
            }
        }
    }

}
