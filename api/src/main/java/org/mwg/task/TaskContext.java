package org.mwg.task;

import org.mwg.Graph;

public interface TaskContext {

    Graph graph();

    long getWorld();

    void setWorld(long world);

    long getTime();

    void setTime(long time);

    Object getVariable(String name);

    String[] getVariablesKeys();

    void setVariable(String name, Object value);

    Object getPreviousResult();

    void setResult(Object actionResult);

    //todo rename
    void setResult(Object actionResult,boolean toFree);

    void next();

    void clean();
}
