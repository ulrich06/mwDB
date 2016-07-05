package org.mwg.core.scheduler;

import org.mwg.plugin.Job;
import org.mwg.plugin.Scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @ignore ts
 */
public class ExecutorScheduler implements Scheduler {

    private ExecutorService service;
    private int _workers = -1;

    public ExecutorScheduler workers(int p_workers) {
        this._workers = p_workers;
        return this;
    }

    @Override
    public void dispatch(final Job job) {
        service.submit(new Runnable() {
            @Override
            public void run() {
                job.run();
            }
        });
    }

    @Override
    public void start() {
        if (_workers == -1) {
            this.service = Executors.newCachedThreadPool();
        } else {
            this.service = Executors.newFixedThreadPool(this._workers);
        }
    }

    @Override
    public void stop() {
        this.service.shutdown();
        this.service = null;
    }

}
