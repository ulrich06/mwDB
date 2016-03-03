package org.mwdb.plugin;

public interface KScheduler {

    void dispatch(KTask task);

    void start();

    void stop();

    void detach();

}
