package org.mwdb;

public interface KDeferCounter {

    /**
     * Count down the counter.
     * This will potentially trigger the callback registered by the then method.
     */
    void count();

    /**
     * Set the callback closure to be called when the waiter counter goes to zero.
     *
     * @param callback result closure
     */
    void then(KCallback callback);

}
