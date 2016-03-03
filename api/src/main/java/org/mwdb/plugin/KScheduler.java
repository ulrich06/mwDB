package org.mwdb.plugin;

import org.mwdb.KCallback;

public interface KScheduler {

    void dispatch(KCallback task);

    void start();

    void stop();

    void detach();

}
