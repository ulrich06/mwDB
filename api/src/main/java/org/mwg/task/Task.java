package org.mwg.task;

import org.mwg.Callback;

public interface Task {

    /**
     * Method to set the task context to a particular world.
     *
     * @param world that hasField to be set into the task context and will be used for next sub tasks.
     * @return this task to chain actions (fluent API)
     */
    Task world(long world);

    /**
     * Method to set the task context to a particular time.
     *
     * @param time that hasField to be set into the task context and will be used for next sub tasks
     * @return this task to chain actions (fluent API)
     */
    Task time(long time);

    /**
     * Method to store the current task result into a named variable
     *
     * @param variableName to store result
     * @return this task to chain actions (fluent API)
     */
    Task asVar(String variableName);

    /**
     * Method to retrieve a previous task result and stack it for next sub tasks.
     *
     * @param variableName to retrieve the past result
     * @return this task to chain actions (fluent API)
     */
    Task fromVar(String variableName);

    /**
     * Method to initialise a task with any object
     *
     * @param inputValue object used as source of a task
     * @return this task to chain actions (fluent API)
     */
    Task from(Object inputValue);

    /**
     * Retrieve an indexed nodes which respect a query
     *
     * @param indexName named of the global index to use
     * @param query     query to retrieve node such as name=FOO
     * @return this task to chain actions (fluent API)
     */
    Task fromIndex(String indexName, String query);

    /**
     * Retrieve all indexed nodes fromVar a global named index
     *
     * @param indexName named of the global index to use
     * @return this task to chain actions (fluent API)
     */
    Task fromIndexAll(String indexName);

    /**
     * @param name
     * @param pattern
     * @return
     */
    Task selectWith(String name, String pattern);

    Task selectWithout(String name, String pattern);

    /**
     * Filter the previous result to get nodes that respect the specified condition in {@code filterFunction}
     * WARNING: Do not do an asynchronous call in the filter function. If you want to do this,
     * please use {@link #selectWhere(Task)}
     * @param filterFunction condition that nodes have to respect
     * @return this task to chain actions (fluent API)
     */
    Task select(TaskFunctionSelect filterFunction);

    /**
     * Filter the previous result to get nodes that respect the specified condition in {@code subTask}
     * Similar to {@link #select(TaskFunctionSelect)}, but allow asynchronous method call
     * @param subTask
     * @return
     */
    Task selectWhere(Task subTask);

    /**
     * Traverse the specified relation
     * If it is followed by {@link #asVar(String)} method, the element are stored in an array
     *
     * @param relationName relation to traverse
     * @return this task to chain actions (fluent API)
     */
    Task traverse(String relationName);

    Task map(TaskFunctionMap mapFunction);

    Task flatMap(TaskFunctionFlatMap flatMapFunction);

    Task group(TaskFunctionGroup groupFunction);

    Task groupWhere(Task groupSubTask);

    Task foreach(Task subTask);

    Task foreachPar(Task subTask);

    <T> Task foreachThen(Callback<T> action);

    /**
     * Execute and wait a sub task, result of this sub task is immediately enqueue and available for next
     *
     * @param subTask that have to be executed
     * @return this task to chain actions (fluent API)
     */
    Task wait(Task subTask);

    Task ifThen(TaskFunctionConditional cond, Task then);

    Task whileDo(TaskFunctionConditional cond, Task then);

    Task then(TaskAction action);

    Task thenAsync(TaskAction action);

    /**
     * Schedule and execute the current task program
     */
    void execute();

    /**
     * Schedule and execute the current task program, when every actions will be execute the last action passed as parameter will be executed.
     * This last action is synchronous, meaning that after the last statement the task is considered over and all intermediate results will be automatically cleaned.
     *
     * @param action last action the execution before the clean procedure
     */
    void executeThen(TaskAction action);

    void executeThenAsync(final TaskContext parentContext, final Object initialResult, final TaskAction finalAction);

}
