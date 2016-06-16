package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.core.utility.PrimitiveHelper;
import org.mwg.plugin.AbstractNode;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CoreTaskContext implements org.mwg.task.TaskContext {

    private final Map<String, Object> _variables;
    private final Object[] _results;
    private final Graph _graph;
    private final TaskAction[] _actions;
    private final AtomicInteger _currentTaskId;
    private final org.mwg.task.TaskContext _parentContext;
    private Object _initialResult;
    private long _world;
    private long _time;

    public CoreTaskContext(final org.mwg.task.TaskContext p_parentContext, final Object p_initialResult, final Graph p_graph, final TaskAction[] p_actions) {
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
    public final long world() {
        return this._world;
    }

    @Override
    public final void setWorld(long p_world) {
        this._world = p_world;
    }

    @Override
    public final long time() {
        return this._time;
    }

    @Override
    public final void setTime(long p_time) {
        this._time = p_time;
    }

    @Override
    public final Object variable(String name) {
        Object result = this._variables.get(name);
        if (result != null) {
            return result;
        }
        if (_parentContext != null) {
            return this._parentContext.variable(name);
        }
        return null;
    }

    @Override
    public String[] variablesKeys() {
        String[] result = new String[this._variables.size()];
        int index = 0;
        for (String key : this._variables.keySet()) {
            result[index++] = key;
        }
        return result;
    }

    @Override
    public final void addToVariable(String name, Object value) {
        Object result = this._variables.get(name);
        if (result == null) {
            Object[] newArr = new Object[1];
            newArr[0] = value;
            this._variables.put(name, newArr);
        } else if (result instanceof Object[]) {
            Object[] previous = (Object[]) result;
            Object[] incArr = new Object[previous.length + 1];
            System.arraycopy(previous, 0, incArr, 0, previous.length);
            incArr[previous.length] = value;
            this._variables.put(name, incArr);
        } else {
            Object[] newArr = new Object[2];
            newArr[0] = result;
            newArr[1] = value;
            this._variables.put(name, newArr);
        }
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
    public final Object result() {
        int current = _currentTaskId.get();
        if (current == 0) {
            return _initialResult;
        } else {
            Object previousResult = _results[current - 1];
            if (previousResult != null && previousResult instanceof org.mwg.core.task.CoreTaskContext) {
                return ((org.mwg.task.TaskContext) previousResult).result();
            } else if (previousResult != null && previousResult instanceof org.mwg.core.task.CoreTaskContext[]) {
                org.mwg.core.task.CoreTaskContext[] contexts = (org.mwg.core.task.CoreTaskContext[]) previousResult;
                Object[] result = new Object[contexts.length];
                int result_index = 0;
                for (int i = 0; i < contexts.length; i++) {
                    Object currentLoop = contexts[i].result();
                    if (currentLoop != null) {
                        result[result_index] = currentLoop;
                        result_index++;
                    }
                }
                if (contexts.length == result_index) {
                    return result;
                } else {
                    Object[] shrinked = new Object[result_index];
                    System.arraycopy(result, 0, shrinked, 0, result_index);
                    return shrinked;
                }
            } else {
                return previousResult;
            }
        }
    }

    @Override
    public final void setResult(Object actionResult) {
        if (actionResult instanceof CoreTaskContext || actionResult instanceof TaskContextWrapper) {
            mergeVariables((TaskContext) actionResult);
        } else if (actionResult instanceof CoreTaskContext[] || actionResult instanceof TaskContextWrapper[]) {
            for (org.mwg.task.TaskContext taskContext : (TaskContext[]) actionResult) {
                mergeVariables(taskContext);
            }
        }
        int i = _currentTaskId.get();
        this._results[i] = actionResult;
    }

    private void mergeVariables(TaskContext actionResult) {
        String[] variables = actionResult.variablesKeys();
        for (String variableName : variables) {
            this.setVariable(variableName, actionResult.variable(variableName));
        }
    }

    @Override
    public final void next() {
        TaskAction nextAction = _actions[_currentTaskId.incrementAndGet()];
        nextAction.eval(this);
    }

    @Override
    public final void clean() {
        if (_initialResult != null) {
            cleanObj(_initialResult);
            _initialResult = null;
        }
        for (int i = 0; i < _results.length; i++) {
            cleanObj(_results[i]);
            _results[i] = null;
        }
    }

    private void cleanObj(Object o) {
        final CoreTaskContext selfPoiner = this;
        if (!PrimitiveHelper.iterate(o, new Callback<Object>() {
            @Override
            public void on(Object result) {
                if (result instanceof AbstractNode) {
                    ((Node) result).free();
                } else if (result instanceof org.mwg.core.task.CoreTaskContext) {
                    ((org.mwg.task.TaskContext) result).clean();
                } else {
                    selfPoiner.cleanObj(result);
                }
            }
        })) {
            if (o instanceof AbstractNode) {
                ((Node) o).free();
            } else if (o instanceof org.mwg.core.task.CoreTaskContext) {
                ((org.mwg.task.TaskContext) o).clean();
            }
        }
    }

}
