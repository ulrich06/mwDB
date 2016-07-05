package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.Constants;
import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.core.task.math.CoreMathExpressionEngine;
import org.mwg.core.task.math.MathExpressionEngine;
import org.mwg.plugin.AbstractNode;
import org.mwg.plugin.Job;
import org.mwg.task.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

public class CoreTask implements org.mwg.task.Task {

    private TaskAction[] _actions = new TaskAction[10];
    private int _actionCursor = 0;

    private void addAction(TaskAction task) {
        if (_actionCursor == _actions.length) {
            TaskAction[] temp_actions = new TaskAction[_actions.length * 2];
            System.arraycopy(_actions, 0, temp_actions, 0, _actions.length);
            _actions = temp_actions;
        }
        _actions[_actionCursor] = task;
        _actionCursor++;
    }

    @Override
    public final org.mwg.task.Task setWorld(String variableName) {
        addAction(new ActionWorld(variableName));
        return this;
    }

    @Override
    public final org.mwg.task.Task setTime(String variableName) {
        addAction(new ActionTime(variableName));
        return this;
    }

    @Override
    public final org.mwg.task.Task fromIndex(String indexName, String query) {
        if (indexName == null) {
            throw new RuntimeException("indexName should not be null");
        }
        if (query == null) {
            throw new RuntimeException("query should not be null");
        }
        addAction(new ActionFromIndex(indexName, query));
        return this;
    }

    @Override
    public final org.mwg.task.Task fromIndexAll(String indexName) {
        if (indexName == null) {
            throw new RuntimeException("indexName should not be null");
        }
        addAction(new ActionFromIndexAll(indexName));
        return this;
    }

    @Override
    public final org.mwg.task.Task selectWith(String name, String pattern) {
        if (pattern == null) {
            throw new RuntimeException("pattern should not be null");
        }
        addAction(new ActionWith(name, Pattern.compile(pattern)));
        return this;
    }

    @Override
    public final org.mwg.task.Task selectWithout(String name, String pattern) {
        if (pattern == null) {
            throw new RuntimeException("pattern should not be null");
        }
        addAction(new ActionWithout(name, Pattern.compile(pattern)));
        return this;
    }

    @Override
    public final org.mwg.task.Task asVar(String variableName) {
        if (variableName == null) {
            throw new RuntimeException("variableName should not be null");
        }
        addAction(new ActionAsVar(variableName));
        return this;
    }


    @Override
    public final org.mwg.task.Task fromVar(String variableName) {
        if (variableName == null) {
            throw new RuntimeException("variableName should not be null");
        }
        addAction(new ActionFromVar(variableName));
        return this;
    }

    @Override
    public Task setVar(String variableName, Object inputValue) {
        if (variableName == null) {
            throw new RuntimeException("variableName should not be null");
        }
        if (inputValue == null) {
            throw new RuntimeException("inputValue should not be null");
        }
        addAction(new ActionSetVar(variableName, inputValue));
        return this;
    }

    @Override
    public final org.mwg.task.Task select(TaskFunctionSelect filter) {
        if (filter == null) {
            throw new RuntimeException("filter should not be null");
        }
        addAction(new ActionSelect(filter));
        return this;
    }

    @Override
    public final org.mwg.task.Task selectWhere(org.mwg.task.Task subTask) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public final Task get(String name) {
        addAction(new ActionGet(name));
        return this;
    }

    @Override
    public final org.mwg.task.Task traverse(String relationName) {
        addAction(new ActionTraverse(relationName));
        return this;
    }

    @Override
    public final Task traverseOrKeep(String relationName) {
        addAction(new ActionTraverseOrKeep(relationName));
        return this;
    }

    @Override
    public final org.mwg.task.Task traverseIndex(String indexName, String query) {
        if (indexName == null) {
            throw new RuntimeException("indexName should not be null");
        }
        addAction(new ActionTraverseIndex(indexName, query));
        return this;
    }

    @Override
    public final org.mwg.task.Task traverseIndexAll(String indexName) {
        if (indexName == null) {
            throw new RuntimeException("indexName should not be null");
        }
        addAction(new ActionTraverseIndex(indexName, null));
        return this;
    }

    @Override
    public final org.mwg.task.Task map(TaskFunctionMap mapFunction) {
        if (mapFunction == null) {
            throw new RuntimeException("mapFunction should not be null");
        }
        addAction(new ActionMap(mapFunction));
        return this;
    }

    @Override
    public final org.mwg.task.Task flatMap(TaskFunctionFlatMap flatMapFunction) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public final org.mwg.task.Task group(TaskFunctionGroup groupFunction) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public final org.mwg.task.Task groupWhere(Task groupSubTask) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public final org.mwg.task.Task inject(Object inputValue) {
        if (inputValue == null) {
            throw new RuntimeException("inputValue should not be null");

        }
        addAction(new ActionInject(inputValue));
        return this;
    }

    @Override
    public final org.mwg.task.Task executeSubTask(Task subTask) {
        if (subTask == null) {
            throw new RuntimeException("subTask should not be null");
        }
        addAction(new ActionTrigger(subTask));
        return this;
    }

    @Override
    public final org.mwg.task.Task ifThen(TaskFunctionConditional cond, Task then) {
        if (cond == null) {
            throw new RuntimeException("condition should not be null");
        }
        if (then == null) {
            throw new RuntimeException("subTask should not be null");
        }
        addAction(new ActionIfThen(cond, then));
        return this;
    }

    @Override
    public final org.mwg.task.Task whileDo(TaskFunctionConditional cond, org.mwg.task.Task then) {
        //addAction(new ActionWhileDo(cond, then));
        //return this;
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public final org.mwg.task.Task then(Action p_action) {
        if (p_action == null) {
            throw new RuntimeException("action should not be null");
        }
        addAction(new ActionWrapper(p_action));
        return this;
    }

    @Override
    public final org.mwg.task.Task foreach(Task subTask) {
        if (subTask == null) {
            throw new RuntimeException("subTask should not be null");
        }
        addAction(new ActionForeach(subTask));
        return this;
    }

    @Override
    public final org.mwg.task.Task foreachPar(Task subTask) {
        if (subTask == null) {
            throw new RuntimeException("subTask should not be null");
        }
        addAction(new ActionForeachPar(subTask));
        return this;
    }

    @Override
    public Task save() {
        addAction(new ActionSave());
        return this;
    }

    @Override
    public Task lookup(String world, String time, String id) {
        addAction(new ActionLookup(world, time, id));
        return this;
    }

    @Override
    public void executeWith(final Graph graph, final Map<String, Object> variables, final Object initialResult, final Callback<Object> result) {
        if (_actionCursor == 0) {
            if (result != null) {
                result.on(protect(graph, initialResult));
            }
        } else {
            final org.mwg.task.TaskContext context = new CoreTaskContext(variables, protect(graph, initialResult), graph, _actions, _actionCursor, result);
            graph.scheduler().dispatch(new Job() {
                @Override
                public void run() {
                    _actions[0].eval(context);
                }
            });
        }
    }

    @Override
    public void execute(Graph graph, Callback<Object> result) {
        executeWith(graph, null, null, result);
    }

    @Override
    public Task action(String name, String flatParams) {
        if (name == null) {
            throw new RuntimeException("name should not be null");
        }
        if (flatParams == null) {
            throw new RuntimeException("flatParams should not be null");
        }
        addAction(new ActionPlugin(name, flatParams));
        return this;
    }

    @Override
    public Task parse(final String flat) {
        if (flat == null) {
            throw new RuntimeException("flat should not be null");
        }
        int cursor = 0;
        int flatSize = flat.length();
        int previous = 0;
        String actionName = null;
        boolean isClosed = false;
        boolean isEscaped = false;
        while (cursor < flatSize) {
            char current = flat.charAt(cursor);
            switch (current) {
                case '\'':
                    isEscaped = true;
                    while (cursor < flatSize) {
                        if (flat.charAt(cursor) == '\'') {
                            break;
                        }
                        cursor++;
                    }
                    break;
                case Constants.TASK_SEP:
                    if (!isClosed) {
                        String getName = flat.substring(previous, cursor);
                        action("get", getName);//default action
                    }
                    actionName = null;
                    isEscaped = false;
                    previous = cursor + 1;
                    break;
                case Constants.TASK_PARAM_OPEN:
                    actionName = flat.substring(previous, cursor);
                    previous = cursor + 1;
                    break;
                case Constants.TASK_PARAM_CLOSE:
                    //ADD LAST PARAM
                    String extracted;
                    if (isEscaped) {
                        extracted = flat.substring(previous + 1, cursor - 1);
                    } else {
                        extracted = flat.substring(previous, cursor);
                    }
                    action(actionName, extracted);
                    actionName = null;
                    previous = cursor + 1;
                    isClosed = true;
                    //ADD TASK
                    break;
            }
            cursor++;
        }
        if (!isClosed) {
            String getName = flat.substring(previous, cursor);
            if (getName.length() > 0) {
                action("get", getName);//default action
            }
        }
        return this;
    }

    public static Object protect(final Graph graph, final Object input) {
        if (input instanceof AbstractNode) {
            return graph.cloneNode((Node) input);
        } else if (input instanceof Object[]) {
            Object[] casted = (Object[]) input;
            Object[] cloned = new Object[casted.length];
            boolean isAllNode = true;
            for (int i = 0; i < casted.length; i++) {
                cloned[i] = protect(graph, casted[i]);
                isAllNode = isAllNode && (cloned[i] instanceof AbstractNode);
            }
            if (isAllNode) {
                Node[] typedResult = new Node[cloned.length];
                System.arraycopy(cloned, 0, typedResult, 0, cloned.length);
                return typedResult;
            }
            return cloned;
        } else {
            return protectIterable(input);
        }
    }

    /**
     * @native ts
     * if(input != null && input != undefined && input['iterator'] != undefined){
     * var flat = [];
     * var it = input['iterator']();
     * while(it.hasNext()){
     * flat.push(it.next());
     * }
     * return flat;
     * } else {
     * return input;
     * }
     */
    private static Object protectIterable(Object input) {
        if (input instanceof Collection) {
            Collection casted = (Collection) input;
            Object[] flat = new Object[casted.size()];
            int flat_index = 0;
            Iterator it = casted.iterator();
            while (it.hasNext()) {
                flat[flat_index] = it.next();
                flat_index++;
            }
            return flat;
        }

        return input;
    }

    @Override
    public Task newNode() {
        addAction(new ActionNewNode());
        return this;
    }

    @Override
    public Task setProperty(String propertyName, byte propertyType, String variableNameToSet) {
        if (propertyName == null) {
            throw new RuntimeException("propertyName should not be null");
        }
        if (variableNameToSet == null) {
            throw new RuntimeException("variableNameToSet should not be null");
        }
        addAction(new ActionSetProperty(propertyName, propertyType, variableNameToSet));
        return this;
    }

    @Override
    public Task removeProperty(String propertyName) {
        if (propertyName == null) {
            throw new RuntimeException("propertyName should not be null");
        }
        addAction(new ActionRemoveProperty(propertyName));
        return this;
    }

    @Override
    public Task add(String relationName, String variableNameToAdd) {
        if (relationName == null) {
            throw new RuntimeException("relationName should not be null");
        }
        if (variableNameToAdd == null) {
            throw new RuntimeException("relatedNode should not be null");
        }
        addAction(new ActionAdd(relationName, variableNameToAdd));
        return this;
    }

    @Override
    public Task remove(String relationName, String variableNameToRemove) {
        if (relationName == null) {
            throw new RuntimeException("relationName should not be null");
        }
        if (variableNameToRemove == null) {
            throw new RuntimeException("variableNameToRemove should not be null");
        }
        addAction(new ActionRemove(relationName, variableNameToRemove));
        return this;
    }

    @Override
    public Task math(String expression) {
        addAction(new ActionMath(expression));
        return this;
    }

    @Override
    public Task split(String splitPattern) {
        addAction(new ActionSplit(splitPattern));
        return this;
    }

    @Override
    public Task repeat(int repetition, Task subTask) {
        addAction(new ActionRepeat(repetition, subTask));
        return this;
    }

    @Override
    public Task repeatPar(int repetition, Task subTask) {
        throw new RuntimeException("Not implemented yet!");
    }

    @Override
    public Task printResult() {
        addAction(new ActionPrintResult());
        return this;
    }

    public static void fillDefault(Map<String, TaskActionFactory> registry) {
        registry.put("get", new TaskActionFactory() { //DefaultTask
            @Override
            public TaskAction create(String[] params) {
                if (params.length != 1) {
                    throw new RuntimeException("get action need one parameter");
                }
                return new ActionGet(params[0]);
            }
        });
        registry.put("math", new TaskActionFactory() { //DefaultTask
            @Override
            public TaskAction create(String[] params) {
                if (params.length != 1) {
                    throw new RuntimeException("math action need one parameter");
                }
                return new ActionMath(params[0]);
            }
        });
        registry.put("traverse", new TaskActionFactory() {
            @Override
            public TaskAction create(String[] params) {
                if (params.length != 1) {
                    throw new RuntimeException("traverse action need one parameter");
                }
                return new ActionTraverse(params[0]);
            }
        });
        registry.put("traverseOrKeep", new TaskActionFactory() {
            @Override
            public TaskAction create(String[] params) {
                if (params.length != 1) {
                    throw new RuntimeException("traverseOrKeep action need one parameter");
                }
                return new ActionTraverseOrKeep(params[0]);
            }
        });
        registry.put("fromIndexAll", new TaskActionFactory() {
            @Override
            public TaskAction create(String[] params) {
                if (params.length != 1) {
                    throw new RuntimeException("fromIndexAll action need one parameter");
                }
                return new ActionFromIndexAll(params[0]);
            }
        });
        registry.put("fromIndex", new TaskActionFactory() {
            @Override
            public TaskAction create(String[] params) {
                if (params.length != 2) {
                    throw new RuntimeException("fromIndex action need two parameter");
                }
                return new ActionFromIndex(params[0], params[1]);
            }
        });
        registry.put("with", new TaskActionFactory() {
            @Override
            public TaskAction create(String[] params) {
                if (params.length != 2) {
                    throw new RuntimeException("with action need two parameter");
                }
                return new ActionWith(params[0], Pattern.compile(params[1]));
            }
        });
        registry.put("without", new TaskActionFactory() {
            @Override
            public TaskAction create(String[] params) {
                if (params.length != 2) {
                    throw new RuntimeException("without action need two parameter");
                }
                return new ActionWithout(params[0], Pattern.compile(params[1]));
            }
        });
    }

}
