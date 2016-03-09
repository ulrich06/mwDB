package org.mwdb;

/**
 * KDeferCounter provides a mean to wait for an amount of events before running a method.
 */
public interface KDeferCounter {

    /**
     * Notifies the counter that an awaited event has occurred.
     * If the total amount of awaited events is reached, the task registered by the {@link #then(KCallback) then} method is executed.
     */
    void count();

    /**
     * Registers the task, in form of a {@link KCallback}, to be called when all awaited events have occurred.
     *
     * @param callback The task to be executed
     */
    void then(KCallback callback);

}
