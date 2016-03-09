package org.mwdb;

public interface KCallback<A> {

    /**
     * This method is build to deliver an asynchronous result.
     * In Java 8, Typescript and JavaScript, this method can be used as a closure.
     *
     * @param result result object when ready
     */
    void on(A result);

}
