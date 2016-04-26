package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.plugin.Job;
import org.mwg.task.*;

public class Task implements org.mwg.task.Task {

    private final Graph _graph;

    private TaskAction[] _actions = new TaskAction[10];
    private int _actionCursor = 0;

    public Task(final Graph p_graph) {
        this._graph = p_graph;
    }

    private void addTask(TaskAction task) {
        if (_actionCursor == _actions.length) {
            TaskAction[] temp_actions = new TaskAction[_actions.length * 2];
            System.arraycopy(_actions, 0, temp_actions, 0, _actions.length);
            _actions = temp_actions;
        }
        _actions[_actionCursor] = task;
        _actionCursor++;
    }

    @Override
    public org.mwg.task.Task world(long world) {
        addTask(new ActionWorld(world));
        return this;
    }

    @Override
    public org.mwg.task.Task time(long time) {
        addTask(new ActionTime(time));
        return this;
    }

    @Override
    public org.mwg.task.Task fromIndex(String indexName, String query) {
        addTask(new ActionFromIndex(indexName, query));
        return this;
    }

    @Override
    public org.mwg.task.Task fromIndexAll(String indexName) {
        addTask(new ActionFromIndexAll(indexName));
        return this;
    }

    @Override
    public org.mwg.task.Task selectWith(String name, String pattern) {
        addTask(new ActionWith(name, pattern));
        return this;
    }

    @Override
    public org.mwg.task.Task selectWithout(String name, String pattern) {
        addTask(new ActionWithout(name, pattern));
        return this;
    }

    @Override
    public org.mwg.task.Task asVar(String variableName) {
        addTask(new ActionAsVar(variableName));
        return this;
    }

    @Override
    public org.mwg.task.Task fromVar(String variableName) {
        addTask(new ActionFromVar(variableName));
        return this;
    }

    @Override
    public org.mwg.task.Task select(TaskFunctionSelect filter) {
        addTask(new ActionSelect(filter));
        return this;
    }

    @Override
    public org.mwg.task.Task selectWhere(org.mwg.task.Task subTask) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public org.mwg.task.Task traverse(String relationName) {
        addTask(new ActionTraverse(relationName));
        return this;
    }

    @Override
    public org.mwg.task.Task traverseIndex(String indexName, String query) {
        addTask(new ActionTraverseIndex(indexName,query));
        return this;
    }

    @Override
    public org.mwg.task.Task traverseIndexAll(String indexName) {
        addTask(new ActionTraverseIndex(indexName,null));
        return this;
    }

    @Override
    public org.mwg.task.Task map(TaskFunctionMap mapFunction) {
        addTask(new ActionMap(mapFunction));
        return this;
    }

    @Override
    public org.mwg.task.Task flatMap(TaskFunctionFlatMap flatMapFunction) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public org.mwg.task.Task group(TaskFunctionGroup groupFunction) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public org.mwg.task.Task groupWhere(org.mwg.task.Task groupSubTask) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public org.mwg.task.Task from(Object inputValue) {
        addTask(new ActionFrom(inputValue));
        return this;
    }

    @Override
    public org.mwg.task.Task wait(org.mwg.task.Task subTask) {
        addTask(new ActionTrigger(subTask));
        return this;
    }

    @Override
    public org.mwg.task.Task ifThen(TaskFunctionConditional cond, org.mwg.task.Task then) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public org.mwg.task.Task whileDo(TaskFunctionConditional cond, org.mwg.task.Task then) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public org.mwg.task.Task then(TaskAction p_action) {
        addTask(new ActionWrapper(p_action));
        return this;
    }

    @Override
    public org.mwg.task.Task thenAsync(TaskAction p_action) {
        addTask(p_action);
        return this;
    }

    @Override
    public void execute() {
        executeThenAsync(null, null, null);
    }

    @Override
    public void executeThen(TaskAction p_action) {
        executeThenAsync(null, null, new ActionWrapper(p_action));
    }

    @Override
    public <T> org.mwg.task.Task foreachThen(Callback<T> action) {
        org.mwg.task.Task task = _graph.newTask().then(new TaskAction() {
            @Override
            public void eval(org.mwg.task.TaskContext context) {
                Object previousResult = context.getPreviousResult();
                if (previousResult != null) {
                    action.on((T) previousResult);
                }
            }
        });
        foreach(task);
        return this;
    }

    @Override
    public org.mwg.task.Task foreach(org.mwg.task.Task subTask) {
        addTask(new ActionForeach(subTask));
        return this;
    }

    @Override
    public org.mwg.task.Task foreachPar(org.mwg.task.Task subTask) {
        addTask(new ActionForeachPar(subTask));
        return this;
    }

    @Override
    public void executeThenAsync(final org.mwg.task.TaskContext parent, final Object initialResult, final TaskAction p_finalAction) {
        final TaskAction[] final_actions = new TaskAction[_actionCursor + 2];
        System.arraycopy(_actions, 0, final_actions, 0, _actionCursor);
        if (p_finalAction != null) {
            final_actions[_actionCursor] = p_finalAction;
        } else {
            final_actions[_actionCursor] = new ActionNoop();
        }
        final_actions[_actionCursor + 1] = new TaskAction() {
            @Override
            public void eval(org.mwg.task.TaskContext context) {
                context.clean();
            }
        };
        final org.mwg.task.TaskContext context = new TaskContext(parent, initialResult, _graph, final_actions);
        _graph.scheduler().dispatch(new Job() {
            @Override
            public void run() {
                TaskAction first = final_actions[0];
                first.eval(context);
            }
        });
    }


}
