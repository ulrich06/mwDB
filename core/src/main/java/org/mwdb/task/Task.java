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
    public KTask globalFind(String indexName, String query) {
        addTask(new ActionGlobalFind(indexName, query));
        return this;
    }

    @Override
    public KTask globalAll(String indexName) {
        addTask(new ActionGlobalAll(indexName));
        return this;
    }

    @Override
    public KTask with(String name, String pattern) {
        addTask(new ActionWith(name, pattern));
        return this;
    }

    @Override
    public KTask without(String name, String pattern) {
        addTask(new ActionWithout(name, pattern));
        return this;
    }

    @Override
    public KTask has(String name) {
        addTask(new ActionHas(name));
        return this;
    }

    @Override
    public KTask count() {
        addTask(new ActionCount());
        return this;
    }

    @Override
    public KTask as(String variableName) {
        addTask(new ActionAs(variableName));
        return this;
    }

    @Override
    public KTask from(String variableName) {
        addTask(new ActionFrom(variableName));
        return this;
    }

    @Override
    public KTask filter(KTaskFilter filter) {
        addTask(new ActionFilter(filter));
        return this;
    }

    @Override
    public KTask relation(String relation) {
        addTask(new ActionRelation(relation));
        return this;
    }

    @Override
    public KTask input(Object inputValue) {
        addTask(new ActionInput(inputValue));
        return this;
    }

    @Override
    public KTask sub(KTask subTask) {
        addTask(new ActionSub(subTask));
        return this;
    }

    @Override
    public KTask then(KTaskAction p_action) {
        addTask(new ActionWrapper(p_action));
        return this;
    }

    @Override
    public KTask asyncThen(KTaskAction p_action) {
        addTask(p_action);
        return this;
    }

    @Override
    public void execute() {
        executeAsyncThen(null, null, null);
    }

    @Override
    public void executeThen(KTaskAction p_action) {
        executeAsyncThen(null, null, new ActionWrapper(p_action));
    }

    @Override
    public KTask foreach(KTask subTask) {
        addTask(new ActionForeach(subTask));
        return this;
    }

    @Override
    public KTask pforeach(KTask subTask) {
        addTask(new ActionParForeach(subTask));
        return this;
    }

    @Override
    public KTask where(KTask subTask) {
        return null;
    }

    @Override
    public KTask values(String name) {
        return this;
    }

    @Override
    public void executeAsyncThen(final KTaskContext parent, final Object initialResult, final KTaskAction p_finalAction) {

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
