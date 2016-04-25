package org.mwg.core;

import org.mwg.plugin.Job;
import org.mwg.plugin.Scheduler;

public class NoopScheduler implements Scheduler {

    @Override
    public void dispatch(Job job) {
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
