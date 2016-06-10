package org.mwg.task;

import org.mwg.Graph;

public interface TaskContext {

    Graph graph();

    long world();

    void setWorld(long world);

    long time();

    void setTime(long time);

    Object variable(String name);

    String[] variablesKeys();

    void setVariable(String name, Object value);

    void addToVariable(String name, Object value);

    Object result();

    void setResult(Object actionResult);

    void next();

    void clean();

}
