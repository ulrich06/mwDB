package org.mwdb.manager;

import org.mwdb.plugin.KScheduler;
import org.mwdb.plugin.KTask;

public class NoopScheduler implements KScheduler {

    @Override
    public void dispatch(KTask task) {
        task.run();
    }

    @Override
    public void start() {
        //noop
    }

    @Override
    public void stop() {
        //noop
    }

    @Override
    public void detach() {
        //noop
    }
}
