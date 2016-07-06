package org.mwg.core.scheduler;

import org.mwg.plugin.Job;
import org.mwg.plugin.Scheduler;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ignore ts
 * boing,boing,boing....
 */
public class TrampolineScheduler implements Scheduler {

    private final BlockingDeque<Job> queue = new LinkedBlockingDeque<Job>();
    private final AtomicInteger wip = new AtomicInteger();

    @Override
    public void dispatch(Job job) {
        queue.add(job);
        if (wip.getAndIncrement() == 0) {
            do {
                final Job polled = queue.poll();
                if (polled != null) {
                    polled.run();
                }
            } while (wip.decrementAndGet() > 0);
        }
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

}
