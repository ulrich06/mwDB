package org.mwdb.plugin;

import org.mwdb.KCallback;

/**
 * In charge of the scheduling of tasks in mwDB
 */
public interface KScheduler {


    /**
     * Registers a task for execution.
     *
     * @param task The new task to execute.
     */
    void dispatch(KCallback task);

    /**
     * Starts the scheduler (i.e.: the execution of tasks).
     */
    void start();


    /**
     * Terminates the scheduler (i.e.: the execution of tasks).
     */
    void stop();

    void detach();

}
