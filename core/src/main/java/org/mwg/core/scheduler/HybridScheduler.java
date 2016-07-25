package org.mwg.core.scheduler;

import org.mwg.plugin.Job;
import org.mwg.plugin.Scheduler;
import org.mwg.plugin.SchedulerAffinity;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ignore ts
 */
public class HybridScheduler implements Scheduler {

    private Worker[] workers = null;
    private final BlockingDeque<Job> globalQueue = new LinkedBlockingDeque<Job>();
    private int _workers = -1;

    public HybridScheduler workers(int p_workers) {
        this._workers = p_workers;
        return this;
    }

    @Override
    public void dispatch(byte affinity, Job job) {
        switch (affinity) {
            case SchedulerAffinity.SAME_THREAD:
                final Thread currentThread = Thread.currentThread();
                if (Thread.currentThread() instanceof Worker) {
                    final Worker currentWorker = (Worker) currentThread;
                    currentWorker.dispatch(job);
                } else {
                    globalQueue.add(job);
                }
                break;
            default:
                globalQueue.add(job);
                break;
        }
    }

    @Override
    public void start() {
        int nbcore = this._workers;
        if (nbcore == -1) {
            nbcore = Runtime.getRuntime().availableProcessors();
        }
        workers = new Worker[nbcore];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker();
            workers[i].start();
        }
    }

    @Override
    public void stop() {
        if (workers != null) {
            for (int i = 0; i < workers.length; i++) {
                workers[i].running = false;
            }
            workers = null;
        }
    }

    private final class Worker extends Thread {

        private final JobQueue localQueue = new JobQueue();
        private final AtomicInteger wip = new AtomicInteger();
        private boolean running = true;

        Worker() {
            setDaemon(false);
        }

        @Override
        public void run() {
            while (running) {
                final Job globalPolled = globalQueue.poll();
                if (globalPolled != null) {
                    try {
                        globalPolled.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void dispatch(Job job) {
            localQueue.add(job);
            if (wip.getAndIncrement() == 0) {
                do {
                    final Job polled = localQueue.poll();
                    if (polled != null) {
                        polled.run();
                    }
                } while (wip.decrementAndGet() > 0);
            }
        }

    }
    
}
