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

public class CoreTaskContext implements TaskContext {

    private final Map<String, Object> _variables;
    private final Graph _graph;
    private final TaskAction[] _actions;
    private final AtomicInteger _currentTaskId;
    private final TaskContext _parentContext;
    private final Callback<Object> _callback;

    //Mutable current result handler
    private Object _result;
    private long _world;
    private long _time;

    public CoreTaskContext(final TaskContext p_parentContext, final Object initial, final Graph p_graph, final TaskAction[] p_actions, final Callback<Object> p_callback) {
        this._world = 0;
        this._time = 0;
        this._graph = p_graph;
        this._parentContext = p_parentContext;
        this._variables = new ConcurrentHashMap<String, Object>();
        this._result = initial;
        this._actions = p_actions;
        this._callback = p_callback;
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
    public final void setVariable(String name, Object value) {
        final Object previous = this._variables.get(name);
        if (value != null) {
            Object protectedVar = CoreTask.protect(_graph, value);
            this._variables.put(name, protectedVar);
        } else {
            this._variables.remove(name);
        }
        cleanObj(previous);
    }

    @Override
    public final void addToVariable(final String name, final Object value) {
        final Object result = this._variables.get(name);
        final Object protectedVar = CoreTask.protect(_graph, value);
        if (result == null) {
            final Object[] newArr = new Object[1];
            newArr[0] = protectedVar;
            this._variables.put(name, newArr);
        } else if (result instanceof Object[]) {
            final Object[] previous = (Object[]) result;
            final Object[] incArr = new Object[previous.length + 1];
            System.arraycopy(previous, 0, incArr, 0, previous.length);
            incArr[previous.length] = protectedVar;
            this._variables.put(name, incArr);
        } else {
            final Object[] newArr = new Object[2];
            newArr[0] = result;
            newArr[1] = protectedVar;
            this._variables.put(name, newArr);
        }
    }

    @Override
    public final Object result() {
        return this._result;
    }

    @Override
    public String resultAsString() {
        return (String) result();
    }

    @Override
    public String[] resultAsStringArray() {
        return (String[]) result();
    }

    @Override
    public Node resultAsNode() {
        return (Node) result();
    }

    @Override
    public Node[] resultAsNodeArray() {
        return (Node[]) result();
    }

    @Override
    public Object[] resultAsObjectArray() {
        return (Object[]) result();
    }

    @Override
    public final void setResult(Object actionResult) {
        final Object protectedVar = CoreTask.protect(_graph, actionResult);
        final Object previousResult = this._result;
        this._result = protectedVar;
        cleanObj(previousResult); //clean the previous result
        //next step now...
        TaskAction nextAction = _actions[_currentTaskId.incrementAndGet()];
        if (nextAction == null) {
            Object protectResult = null;
            if (this._callback != null) {
                protectResult = CoreTask.protect(_graph, result());
            }

            /* Clean */
            cleanObj(this._result);
            String[] variables = _variables.keySet().toArray(new String[_variables.keySet().size()]);
            for (int i = 0; i < variables.length; i++) {
                cleanObj(variable(variables[i]));
            }
            this._result = null;
            /* End Clean */

            if (this._callback != null) {
                this._callback.on(protectResult);
            }
        } else {
            nextAction.eval(this);
        }
    }

    private void cleanObj(Object o) {
        final CoreTaskContext selfPoiner = this;
        if (!PrimitiveHelper.iterate(o, new Callback<Object>() {
            @Override
            public void on(Object result) {
                if (result instanceof AbstractNode) {
                    ((Node) result).free();
                } else {
                    selfPoiner.cleanObj(result);
                }
            }
        })) {
            if (o instanceof AbstractNode) {
                ((Node) o).free();
            }
        }
    }

}
