package org.mwdb.task;

import org.mwdb.*;
import org.mwdb.plugin.KScheduler;
import org.mwdb.task.action.*;

public class Task implements KTask {

    private final KGraph _graph;

    private KTaskAction[] _actions = new KTaskAction[10];
    private int _actionCursor = 0;

    public Task(final KGraph p_graph) {
        this._graph = p_graph;
    }

    private void addTask(KTaskAction task) {
        if (_actionCursor == _actions.length) {
            KTaskAction[] temp_actions = new KTaskAction[_actions.length * 2];
            System.arraycopy(_actions, 0, temp_actions, 0, _actions.length);
            _actions = temp_actions;
        }
        _actions[_actionCursor] = task;
        _actionCursor++;
    }

    @Override
    public KTask world(long world) {
        addTask(new ActionWorld(world));
        return this;
    }

    @Override
    public KTask time(long time) {
        addTask(new ActionTime(time));
        return this;
    }

    @Override
    public KTask fromIndex(String indexName, String query) {
        addTask(new ActionFromIndex(indexName, query));
        return this;
    }

    @Override
    public KTask fromIndexAll(String indexName) {
        addTask(new ActionFromIndexAll(indexName));
        return this;
    }

    @Override
    public KTask selectWith(String name, String pattern) {
        addTask(new ActionWith(name, pattern));
        return this;
    }

    @Override
    public KTask selectWithout(String name, String pattern) {
        addTask(new ActionWithout(name, pattern));
        return this;
    }

    @Override
    public KTask asVar(String variableName) {
        addTask(new ActionAsVar(variableName));
        return this;
    }

    @Override
    public KTask fromVar(String variableName) {
        addTask(new ActionFromVar(variableName));
        return this;
    }

    @Override
    public KTask select(KTaskSelect filter) {
        addTask(new ActionSelect(filter));
        return this;
    }

    @Override
    public KTask selectWhere(KTask subTask) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public KTask traverse(String relationName) {
        addTask(new ActionTraverse(relationName));
        return this;
    }

    @Override
    public KTask map(KTaskMap mapFunction) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public KTask flatMap(KTaskFlatMap flatMapFunction) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public KTask group(KTaskGroup groupFunction) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public KTask groupWhere(KTask groupSubTask) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public KTask from(Object inputValue) {
        addTask(new ActionFrom(inputValue));
        return this;
    }

    @Override
    public KTask wait(KTask subTask) {
        addTask(new ActionTrigger(subTask));
        return this;
    }

    @Override
    public KTask then(KTaskAction p_action) {
        addTask(new ActionWrapper(p_action));
        return this;
    }

    @Override
    public KTask thenAsync(KTaskAction p_action) {
        addTask(p_action);
        return this;
    }

    @Override
    public void execute() {
        executeThenAsync(null, null, null);
    }

    @Override
    public void executeThen(KTaskAction p_action) {
        executeThenAsync(null, null, new ActionWrapper(p_action));
    }

    @Override
    public KTask foreach(KTask subTask) {
        addTask(new ActionForeach(subTask));
        return this;
    }

    @Override
    public KTask foreachPar(KTask subTask) {
        addTask(new ActionParForeach(subTask));
        return this;
    }

    @Override
    public void executeThenAsync(final KTaskContext parent, final Object initialResult, final KTaskAction p_finalAction) {
        final KTaskAction[] final_actions = new KTaskAction[_actionCursor + 2];
        System.arraycopy(_actions, 0, final_actions, 0, _actionCursor);
        if (p_finalAction != null) {
            final_actions[_actionCursor] = p_finalAction;
        } else {
            final_actions[_actionCursor] = new ActionNoop();
        }
        final_actions[_actionCursor + 1] = new KTaskAction() {
            @Override
            public void eval(KTaskContext context) {
                context.clean();
            }
        };
        final KTaskContext context = new TaskContext(parent, initialResult, _graph, final_actions);
        _graph.scheduler().dispatch(new KScheduler.KJob() {
            @Override
            public void run() {
                KTaskAction first = final_actions[0];
                first.eval(context);
            }
        });
    }


}
