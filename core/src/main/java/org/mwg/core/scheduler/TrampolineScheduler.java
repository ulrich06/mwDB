package org.mwg.core.scheduler;

import org.mwg.plugin.Job;
import org.mwg.plugin.Scheduler;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * boing,boing,boing....
 */

/** @ignore ts
 */
public class TrampolineScheduler implements Scheduler {


    @Override
    public void dispatch(Job job) {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    private class Worker {
        private final BlockingDeque<Job> queue = new LinkedBlockingDeque<Job>();
        private final AtomicInteger wip = new AtomicInteger();

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
    }

}
