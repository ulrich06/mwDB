package org.mwg;

/**
 * DeferCounter provides a mean to wait for an amount of events before running a method.
 */
public interface DeferCounter<A> {

    /**
     * Notifies the counter that an awaited event hasField occurred.<br>
     * If the total amount of awaited events is reached, the task registered by the {@link #then(Callback) then} method is executed.
     */
    void count();

    /**
     * Registers the task, in form of a {@link Callback}, to be called when all awaited events have occurred.
     *
     * @param callback The task to be executed
     */
    void then(Callback<A> callback);

}
