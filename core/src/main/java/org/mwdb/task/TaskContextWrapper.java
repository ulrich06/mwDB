package org.mwdb.task;

import org.mwdb.KGraph;
import org.mwdb.KTaskContext;

public class TaskContextWrapper implements KTaskContext {

    private final KTaskContext _wrapped;

    public TaskContextWrapper(KTaskContext p_wrapped) {
        _wrapped = p_wrapped;
    }

    @Override
    public KGraph graph() {
        return _wrapped.graph();
    }

    @Override
    public long getWorld() {
        return _wrapped.getWorld();
    }

    @Override
    public void setWorld(long world) {
        _wrapped.setWorld(world);
    }

    @Override
    public long getTime() {
        return _wrapped.getTime();
    }

    @Override
    public void setTime(long time) {
        _wrapped.setTime(time);
    }

    @Override
    public Object getVariable(String name) {
        return _wrapped.getVariable(name);
    }

    @Override
    public void setVariable(String name, Object value) {
        _wrapped.setVariable(name, value);
    }

    @Override
    public Object getPreviousResult() {
        return _wrapped.getPreviousResult();
    }

    @Override
    public void setResult(Object actionResult) {
        _wrapped.setResult(actionResult);
    }

    @Override
    public void next() {
        //noop action here, because the next action will be automatically called
    }

    @Override
    public void clean() {
        _wrapped.clean();
    }
}
