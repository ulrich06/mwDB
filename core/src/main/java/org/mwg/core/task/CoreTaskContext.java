package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.core.task.math.CoreMathExpressionEngine;
import org.mwg.core.task.math.MathExpressionEngine;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;
import org.mwg.task.TaskResultIterator;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

class CoreTaskContext implements TaskContext {

    private final Map<String, TaskResult> _variables;
    private final boolean shouldFreeVar;
    private final Graph _graph;
    private final TaskAction[] _actions;
    private final int _actionCursor;
    private final AtomicInteger _currentTaskId;
    private final Callback<TaskResult> _callback;
    private final boolean verbose;
    private final int _ident;

    //Mutable current result handler
    private TaskResult _result;
    private long _world;
    private long _time;

    CoreTaskContext(final Map<String, TaskResult> p_variables, final TaskResult initial, final Graph p_graph, final TaskAction[] p_actions, final int p_actionCursor, final boolean isVerbose, final int p_ident, final Callback<TaskResult> p_callback) {
        this.verbose = isVerbose;
        this._ident = p_ident;
        this._world = 0;
        this._time = 0;
        this._graph = p_graph;
        if (p_variables != null) {
            this._variables = p_variables;
            shouldFreeVar = false;
        } else {
            this._variables = new ConcurrentHashMap<String, TaskResult>();
            shouldFreeVar = true;
        }
        this._result = initial;
        this._actions = p_actions;
        this._actionCursor = p_actionCursor;
        this._callback = p_callback;
        this._currentTaskId = new AtomicInteger(0);
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
    public final TaskResult variable(String name) {
        return this._variables.get(name);
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
    public final void setVariable(String name, TaskResult value) {
        final TaskResult previous = this._variables.get(name);
        if (value != null) {
            this._variables.put(name, value.clone());
        } else {
            this._variables.remove(name);
        }
        if (previous != null) {
            previous.free();
        }
    }

    @Override
    public final void addToVariable(final String name, final TaskResult value) {
        TaskResult previous = this._variables.get(name);
        if (previous == null) {
            previous = new CoreTaskResult(null, false);
            this._variables.put(name, previous);
        }
        previous.add(value);
    }

    @Override
    public Map<String, TaskResult> variables() {
        return this._variables;
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
        int nextCursor = _currentTaskId.incrementAndGet();
        TaskAction nextAction = null;
        if (nextCursor < _actionCursor) {
            nextAction = _actions[nextCursor];
        }
        if (nextAction == null) {
            /* Clean */
            if (shouldFreeVar) {
                String[] variables = _variables.keySet().toArray(new String[_variables.keySet().size()]);
                for (int i = 0; i < variables.length; i++) {
                    variable(variables[i]).free();
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
            if (verbose) {
                printDebug(nextAction);
            }
            nextAction.eval(this);
        }
    }

    private void printDebug(TaskAction t) {
        for (int i = 0; i < _ident; i++) {
            System.out.print("\t");
        }
        String taskName = t.toString();
        System.out.println(template(taskName));
    }


    void executeFirst() {
        if (verbose) {
            printDebug(_actions[0]);
        }
        _actions[0].eval(this);
    }

    @Override
    public String template(String input) {
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
                    MathExpressionEngine mathEngine = CoreMathExpressionEngine.parse(contextKey.substring(1));
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
    public boolean isVerbose() {
        return this.verbose;
    }
}
