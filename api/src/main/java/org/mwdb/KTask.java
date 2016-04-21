package org.mwdb;

public interface KTask {

    /**
     * Method to set the task context to a particular world.
     *
     * @param world that has to be set into the task context and will be used for next sub tasks.
     * @return this task to chain actions (fluent API)
     */
    KTask world(long world);

    /**
     * Method to set the task context to a particular time.
     *
     * @param time that has to be set into the task context and will be used for next sub tasks
     * @return this task to chain actions (fluent API)
     */
    KTask time(long time);

    /**
     * Method to store the current task result into a named variable
     *
     * @param variableName to store result
     * @return this task to chain actions (fluent API)
     */
    KTask as(String variableName);

    /**
     * Method to retrieve a previous task result and stack it for next sub tasks.
     *
     * @param variableName to retrieve the past result
     * @return this task to chain actions (fluent API)
     */
    KTask from(String variableName);

    /**
     * Retrieve nodes from a global named index using a query
     *
     * @param indexName named of the global index to use
     * @param query     query to retrieve node such as name=FOO
     * @return this task to chain actions (fluent API)
     */
    KTask globalFind(String indexName, String query);

    /**
     * Retrieve all nodes from a global named index
     *
     * @param indexName named of the global index to use
     * @return this task to chain actions (fluent API)
     */
    KTask globalAll(String indexName);

    /**
     * @param name
     * @param pattern
     * @return
     */
    KTask with(String name, String pattern);

    KTask without(String name, String pattern);

    KTask has(String name);

    interface KTaskFilter {
        boolean select(KNode node);
    }

    KTask filter(KTaskFilter filter);

    KTask relation(String name);

    KTask input(Object inputValue);

    /**
     * Count the number of results from previous task
     *
     * @return this task to chain actions (fluent API)
     */
    KTask count();

    /**
     * Schedule and execute the current task program
     */
    void execute();

    void executeThen(KTaskAction action);

    //TODO Advanced user only
    void executeAsyncThen(final KTaskContext parentContext, final Object initialResult, final KTaskAction finalAction);

    KTask then(KTaskAction action);

    //TODO Advanced user only
    KTask asyncThen(KTaskAction action);

    //TODO after this

    KTask foreach(KTask subTask);

    KTask pforeach(KTask subTask);

    KTask where(KTask subTask);

    KTask values(String name);

    /**
     * TODO
     */

    /**
     * Execute and wait a sub task, result of this sub task is immediately enqueue and available for next
     *
     * @param subTask that have to be executed
     * @return this task to chain actions (fluent API)
     */
    KTask sub(KTask subTask);

}
