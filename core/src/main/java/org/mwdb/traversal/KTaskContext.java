package org.mwdb.traversal;

import org.mwdb.KGraph;

public interface KTaskContext {

    KGraph graph();

    long getWorld();

    void setWorld(long world);

    long getTime();

    void setTime(long time);

    Object getResult(int taskId);

    void setResult(int taskId, Object value);

    Object getVariable(String name);

    void setVariable(String name, Object value);

    KAction next(int taskId);

}
