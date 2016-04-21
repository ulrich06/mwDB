package org.mwdb;

public interface KTaskContext {

    KGraph graph();

    long getWorld();

    void setWorld(long world);

    long getTime();

    void setTime(long time);

    Object getVariable(String name);

    void setVariable(String name, Object value);

    Object getPreviousResult();

    void setResult(Object actionResult);

    void next();

}
