package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.core.task.math.CoreMathExpressionEngine;
import org.mwg.core.task.math.MathExpressionEngine;
import org.mwg.core.utility.GenericIterable;
import org.mwg.core.utility.PrimitiveHelper;
import org.mwg.plugin.AbstractIterable;
import org.mwg.plugin.AbstractNode;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

class CoreTaskContext implements TaskContext {

    private final Map<String, Object> _variables;
    private final boolean shouldFreeVar;
    private final Graph _graph;
    private final TaskAction[] _actions;
    private final int _actionCursor;
    private final AtomicInteger _currentTaskId;
    private final TaskContext _parentContext;
    private final Callback<Object> _callback;

    //Mutable current result handler
    private Object _result;
    private long _world;
    private long _time;

    CoreTaskContext(final TaskContext p_parentContext, final Object initial, final Graph p_graph, final TaskAction[] p_actions, final int p_actionCursor, final Callback<Object> p_callback) {
        this._world = 0;
        this._time = 0;
        this._graph = p_graph;
        this._parentContext = p_parentContext;
        if (this._parentContext != null) {
            this._variables = ((CoreTaskContext) p_parentContext)._variables;
            shouldFreeVar = false;
        } else {
            this._variables = new ConcurrentHashMap<String, Object>();
            shouldFreeVar = true;
        }
        this._result = initial;
        this._actions = p_actions;
        this._actionCursor = p_actionCursor;
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
        return this._variables.get(name);
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
    public final void setUnsafeResult(Object actionResult) {
        internal_setResult(actionResult, false);
    }

    @Override
    public final void setResult(Object actionResult) {
        internal_setResult(actionResult, true);
    }

    private void internal_setResult(Object actionResult, boolean safe) {
        final Object previousResult = this._result;
        //Optimization
        if (safe) {
            if (actionResult != previousResult) {
                this._result = CoreTask.protect(_graph, actionResult);
                cleanObj(previousResult); //clean the previous result
                cleanObj(actionResult); //clean the previous result
            }
        } else {
            this._result = actionResult;
        }

        //next step now...
        int nextCursor = _currentTaskId.incrementAndGet();
        TaskAction nextAction = null;
        if (nextCursor < _actionCursor) {
            nextAction = _actions[nextCursor];
        }
        if (nextAction == null) {
            Object protectResult = null;
            if (this._callback != null) {
                Object currentResult = result();
                if (currentResult != null) {
                    protectResult = CoreTask.protect(_graph, currentResult);
                }
            }
            /* Clean */
            cleanObj(this._result);
            if (shouldFreeVar) {
                String[] variables = _variables.keySet().toArray(new String[_variables.keySet().size()]);
                for (int i = 0; i < variables.length; i++) {
                    cleanObj(variable(variables[i]));
                }
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

    @Override
    public void cleanObj(Object o) {
        if (o == null) {
            return;
        } else if (o instanceof AbstractIterable) {
            ((AbstractIterable) o).close();
        } else {
            final CoreTaskContext selfPoiner = this;
            final GenericIterable genericIterable = new GenericIterable(o);
            Object current = genericIterable.next();
            while (current != null) {
                if (current instanceof AbstractNode) {
                    ((Node) current).free();
                } else if (o != current) {
                    selfPoiner.cleanObj(current);
                }
                current = genericIterable.next();
            }
        }
    }

    @Override
    public String template(String input) {
        int cursor = 0;
        StringBuilder buffer = null;
        int previousPos = -1;
        while (cursor < input.length()) {
            char currentChar = input.charAt(cursor);
            char previousChar = '0';
            if (cursor > 0) {
                previousChar = input.charAt(cursor - 1);
            }
            if (currentChar == '{' && previousChar == '{') {
                previousPos = cursor + 1;
            } else if (previousPos != -1 && currentChar == '}' && previousChar == '}') {
                if (buffer == null) {
                    buffer = new StringBuilder();
                    buffer.append(input.substring(0, previousPos - 2));
                }
                String contextKey = input.substring(previousPos, cursor - 1).trim();
                if (contextKey.length() > 0 && contextKey.charAt(0) == '=') {
                    MathExpressionEngine mathEngine = CoreMathExpressionEngine.parse(contextKey.substring(1));
                    buffer.append(mathEngine.eval(null, this, new HashMap<String, Double>()));
                } else {
                    buffer.append(variable(contextKey));
                }
                previousPos = -1;
            } else {
                if (previousPos == -1 && buffer != null) {
                    buffer.append(input.charAt(cursor));
                }
            }
            cursor++;
        }
        if (buffer == null) {
            return input;
        } else {
            return buffer.toString();
        }
    }

}
