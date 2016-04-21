package org.mwdb.manager;

import org.mwdb.plugin.KScheduler;

public class NoopScheduler implements KScheduler {

    @Override
    public void dispatch(KJob job) {
        job.run();
    }

    @Override
    public void start() {
        //noop
    }

    @Override
    public void stop() {
        //noop
    }

}
