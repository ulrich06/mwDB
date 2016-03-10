package org.mwdb;

/**
 * <p>Generic structure to define tasks to be executed when an asynchronous result is released.</p>
 * In Java 8, Typescript and JavaScript, this structure can be replaced by a closure.
 * @param <A> The type of the expected result.
 */
public interface KCallback<A> {

    /**
     * This method is called when an asynchronous result is delivered.
     *
     * @param result The expected result.
     */
    void on(A result);

}
