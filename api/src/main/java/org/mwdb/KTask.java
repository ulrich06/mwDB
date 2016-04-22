package org.mwdb;

public interface KTask {

    /**
     * Method to set the task context to a particular world.
     *
     * @param world that hasField to be set into the task context and will be used for next sub tasks.
     * @return this task to chain actions (fluent API)
     */
    KTask world(long world);

    /**
     * Method to set the task context to a particular time.
     *
     * @param time that hasField to be set into the task context and will be used for next sub tasks
     * @return this task to chain actions (fluent API)
     */
    KTask time(long time);

    /**
     * Method to store the current task result into a named variable
     *
     * @param variableName to store result
     * @return this task to chain actions (fluent API)
     */
    KTask asVar(String variableName);

    /**
     * Method to retrieve a previous task result and stack it for next sub tasks.
     *
     * @param variableName to retrieve the past result
     * @return this task to chain actions (fluent API)
     */
    KTask fromVar(String variableName);

    KTask from(Object inputValue);

    /**
     * Retrieve nodes fromVar a global named index using a query
     *
     * @param indexName named of the global index to use
     * @param query     query to retrieve node such as name=FOO
     * @return this task to chain actions (fluent API)
     */
    KTask fromIndex(String indexName, String query);

    /**
     * Retrieve all nodes fromVar a global named index
     *
     * @param indexName named of the global index to use
     * @return this task to chain actions (fluent API)
     */
    KTask fromIndexAll(String indexName);

    /**
     * @param name
     * @param pattern
     * @return
     */
    KTask selectWith(String name, String pattern);

    KTask selectWithout(String name, String pattern);

    interface KTaskFilter {
        boolean select(KNode node);
    }

    KTask select(KTaskFilter filterFunction);

    KTask selectWhere(KTask subTask);

    KTask traverse(String relationName);

    interface KTaskMap {
        Object map(KNode node);
    }

    KTask map(KTaskMap mapFunction);

    interface KTaskFlatMap {
        Object map(KNode[] nodes);
    }

    KTask flatMap(KTaskFlatMap flatMapFunction);

    interface KTaskGroup {
        long group(KNode nodes);
    }

    KTask group(KTaskGroup groupFunction);

    KTask groupWhere(KTask groupSubTask);

    KTask foreach(KTask subTask);

    KTask foreachParallel(KTask subTask);

    /**
     * Execute and wait a sub task, result of this sub task is immediately enqueue and available for next
     *
     * @param subTask that have to be executed
     * @return this task to chain actions (fluent API)
     */
    KTask trigger(KTask subTask);

    KTask then(KTaskAction action);

    KTask thenAsync(KTaskAction action);

    /**
     * Schedule and execute the current task program
     */
    void execute();

    void executeThen(KTaskAction action);

    void executeThenAsync(final KTaskContext parentContext, final Object initialResult, final KTaskAction finalAction);

}
