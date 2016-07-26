package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.core.task.math.CoreMathExpressionEngine;
import org.mwg.core.task.math.MathExpressionEngine;
import org.mwg.plugin.AbstractTaskAction;
import org.mwg.task.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

class CoreTaskContext implements TaskContext {

    private final Map<String, TaskResult> _globalVariables;
    private final TaskContext _parent;
    private final Graph _graph;
    private final Callback<TaskResult> _callback;
    private final int _ident;

    private Map<String, TaskResult> _localVariables = null;
    private AbstractTaskAction _current;
    TaskResult _result;
    private long _world;
    private long _time;
    private final TaskHook _hook;

    CoreTaskContext(final TaskContext parentContext, final TaskResult initial, final Graph p_graph, final TaskHook p_hook, final int p_ident, final Callback<TaskResult> p_callback) {
        this._hook = p_hook;
        this._ident = p_ident;
        this._world = 0;
        this._time = 0;
        this._graph = p_graph;
        this._parent = parentContext;
        if (parentContext == null) {
            this._globalVariables = new ConcurrentHashMap<String, TaskResult>();
        } else {
            this._globalVariables = _parent.globalVariables();
        }
        this._result = initial;
        this._callback = p_callback;
    }

    @Override
    public int ident() {
        return this._ident;
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
    public final TaskResult variable(final String name) {
        TaskResult resolved = this._globalVariables.get(name);
        if (resolved == null) {
            resolved = internal_deep_resolve(name);
        }
        return resolved;
    }

    private TaskResult internal_deep_resolve(final String name) {
        TaskResult resolved = null;
        if (this._localVariables != null) {
            resolved = this._localVariables.get(name);
        }
        if (resolved == null && this._parent != null) {
            return ((CoreTaskContext) _parent).internal_deep_resolve(name);
        } else {
            return resolved;
        }
    }

    @Override
    public TaskResult wrap(Object input) {
        return new CoreTaskResult(input, false);
    }

    @Override
    public TaskResult wrapClone(Object input) {
        return new CoreTaskResult(input, true);
    }

    @Override
    public TaskResult newResult() {
        return new CoreTaskResult(null, false);
    }

    @Override
    public final void setGlobalVariable(final String name, final TaskResult value) {
        final TaskResult previous = this._globalVariables.put(name, value.clone());
        if (previous != null) {
            previous.free();
        }
    }

    @Override
    public final void setLocalVariable(final String name, final TaskResult value) {
        Map<String, TaskResult> target = internal_deep_resolve_map(name);
        if (target == null) {
            if (this._localVariables == null) {
                this._localVariables = new HashMap<String, TaskResult>();
            }
            target = this._localVariables;
        }
        final TaskResult previous = target.put(name, value.clone());
        if (previous != null) {
            previous.free();
        }
    }

    private Map<String, TaskResult> internal_deep_resolve_map(final String name) {
        if (this._localVariables != null) {
            TaskResult resolved = this._localVariables.get(name);
            if (resolved != null) {
                return this._localVariables;
            }
        }
        if (this._parent != null) {
            return ((CoreTaskContext) _parent).internal_deep_resolve_map(name);
        } else {
            return null;
        }
    }

    @Override
    public final void addToGlobalVariable(final String name, final TaskResult value) {
        TaskResult previous = this._globalVariables.get(name);
        if (previous == null) {
            previous = new CoreTaskResult(null, false);
            this._globalVariables.put(name, previous);
        }
        if (value != null) {
            TaskResult clonedValue = value.clone();
            for (int i = 0; i < clonedValue.size(); i++) {
                previous.add(clonedValue.get(i));
            }
        }
    }

    @Override
    public final void addToLocalVariable(final String name, final TaskResult value) {
        Map<String, TaskResult> target = internal_deep_resolve_map(name);
        if (target == null) {
            if (this._localVariables == null) {
                this._localVariables = new HashMap<String, TaskResult>();
            }
            target = this._localVariables;
        }
        TaskResult previous = target.get(name);
        if (previous == null) {
            previous = new CoreTaskResult(null, false);
            target.put(name, previous);
        }
        if (value != null) {
            TaskResult clonedValue = value.clone();
            for (int i = 0; i < clonedValue.size(); i++) {
                previous.add(clonedValue.get(i));
            }
        }
    }

    @Override
    public Map<String, TaskResult> globalVariables() {
        return this._globalVariables;
    }

    @Override
    public Map<String, TaskResult> localVariables() {
        return this._localVariables;
    }

    @Override
    public final TaskResult result() {
        return this._result;
    }

    @Override
    public TaskResult<Node> resultAsNodes() {
        return (TaskResult<Node>) _result;
    }

    @Override
    public TaskResult<String> resultAsStrings() {
        return (TaskResult<String>) _result;
    }

    @Override
    public final void continueWith(TaskResult nextResult) {
        final TaskResult previousResult = this._result;
        if (previousResult != null) {
            previousResult.free();
        }
        _result = nextResult;
        continueTask();
    }

    @Override
    public final void continueTask() {
        //next step now...
        final AbstractTaskAction previousAction = _current;
        final AbstractTaskAction nextAction = _current.next();
        _current = nextAction;
        if (nextAction == null) {
            /* Clean */
            if (this._localVariables != null) {
                Set<String> localValues = this._localVariables.keySet();
                String[] flatLocalValues = localValues.toArray(new String[localValues.size()]);
                for (int i = 0; i < flatLocalValues.length; i++) {
                    this._localVariables.get(flatLocalValues[i]).free();
                }
            }
            if (this._parent == null) {
                Set<String> globalValues = this._globalVariables.keySet();
                String[] globalFlatValues = globalValues.toArray(new String[globalValues.size()]);
                for (int i = 0; i < globalFlatValues.length; i++) {
                    this._globalVariables.get(globalFlatValues[i]).free();
                }
            }
            /* End Clean */
            if (this._callback != null) {
                this._callback.on(_result);
            } else {
                if (this._result != null) {
                    this._result.free();
                }
            }
        } else {
            if (this._hook != null) {
                this._hook.on(previousAction, nextAction, this);
            }
            nextAction.eval(this);
        }
    }

    final void execute(AbstractTaskAction initialTaskAction) {
        this._current = initialTaskAction;
        if (this._hook != null) {
            this._hook.on(null, _current, this);
        }
        this._current.eval(this);
    }

    @Override
    public final String template(String input) {
        if (input == null) {
            return null;
        }
        int cursor = 0;
        StringBuilder buffer = null;
        int previousPos = -1;
        while (cursor < input.length()) {
            char currentChar = input.charAt(cursor);
            char previousChar = '0';
            char nextChar = '0';
            if (cursor > 0) {
                previousChar = input.charAt(cursor - 1);
            }
            if (cursor + 1 < input.length()) {
                nextChar = input.charAt(cursor + 1);
            }
            if (currentChar == '{' && previousChar == '{') {
                previousPos = cursor + 1;
            } else if (previousPos != -1 && currentChar == '}' && previousChar == '}') {
                if (buffer == null) {
                    buffer = new StringBuilder();
                    buffer.append(input.substring(0, previousPos - 2));
                }
                String contextKey = input.substring(previousPos, cursor - 1).trim();
                if (contextKey.length() > 0 && contextKey.charAt(0) == '=') { //Math expression
                    final MathExpressionEngine mathEngine = CoreMathExpressionEngine.parse(contextKey.substring(1));
                    double value = mathEngine.eval(null, this, new HashMap<String, Double>());
                    //supress ".0" if it exists
                    String valueStr = value + "";
                    for (int i = valueStr.length() - 1; i >= 0; i--) {
                        if (valueStr.charAt(i) == '.') {
                            valueStr = valueStr.substring(0, i);
                            break;
                        } else if (valueStr.charAt(i) != '0') {
                            break;
                        }
                    }
                    buffer.append(valueStr);
                } else {//variable name or array access
                    //check if it is an array access
                    int indexArray = -1;
                    if (contextKey.charAt(contextKey.length() - 1) == ']') {
                        int indexStart = -1;
                        for (int i = contextKey.length() - 3; i >= 0; i--) {
                            if (contextKey.charAt(i) == '[') {
                                indexStart = i + 1;
                                break;
                            }
                        }
                        if (indexStart != -1) {
                            indexArray = TaskHelper.parseInt(contextKey.substring(indexStart, contextKey.length() - 1));
                            contextKey = contextKey.substring(0, indexStart - 1);
                            if (indexArray < 0) {
                                throw new RuntimeException("Array index out of range: " + indexArray);
                            }
                        }
                    }
                    TaskResult foundVar = variable(contextKey);
                    if (foundVar == null && contextKey.equals("result")) {
                        foundVar = result();
                    }
                    if (foundVar != null) {
                        if (foundVar.size() == 1 || indexArray != -1) {
                            //show element of array
                            Object toShow = null;
                            if (indexArray == -1) {
                                toShow = foundVar.get(0);
                            } else {
                                toShow = foundVar.get(indexArray);
                            }
                            buffer.append(toShow);
                        } else {
                            //show all
                            TaskResultIterator it = foundVar.iterator();
                            buffer.append("[");
                            boolean isFirst = true;
                            Object next = it.next();
                            while (next != null) {
                                if (isFirst) {
                                    isFirst = false;
                                } else {
                                    buffer.append(",");
                                }
                                buffer.append(next);
                                next = it.next();
                            }
                            buffer.append("]");
                        }
                    } else {
                        throw new RuntimeException("Variable not found " + contextKey + " in:" + input);
                    }
                }
                previousPos = -1;
            } else {
                if (previousPos == -1 && buffer != null) {
                    //check if we are not opening a {{
                    if (currentChar == '{' && nextChar == '{') {
                        //noop
                    } else {
                        buffer.append(input.charAt(cursor));
                    }
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


    @Override
    public TaskHook hook() {
        return this._hook;
    }
}
