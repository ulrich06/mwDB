package org.mwg.plugin;

/**
 * In charge of the scheduling of tasks in mwDB
 */
public interface Scheduler {

    /**
     * Registers a job for execution.
     *
     * @param job The new job to execute.
     */
    void dispatch(Job job);

    /**
     * Starts the scheduler (i.e.: the execution of tasks).
     */
    void start();
    
    /**
     * Terminates the scheduler (i.e.: the execution of tasks).
     */
    void stop();

}
