package org.mwg.task;

import org.mwg.Graph;
import org.mwg.Node;

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

    //Object based results
    Object result();

    Object[] resultAsObjectArray();

    //String based results
    String resultAsString();

    String[] resultAsStringArray();

    //Node based results
    Node resultAsNode();

    Node[] resultAsNodeArray();

    void setResult(Object actionResult);

    void next();

    void clean();

}
