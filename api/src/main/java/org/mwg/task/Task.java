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
     * If you want to access/modify the context, please use, please use {@link #selectWhere(Task)}
     *
     * @param filterFunction condition that nodes have to respect
     * @return this task to chain actions (fluent API)
     */
    Task select(TaskFunctionSelect filterFunction);

    /**
     * Filter the previous result to get nodes that respect the specified condition in {@code subTask}
     * Similar to {@link #select(TaskFunctionSelect)}, but allow access/modification of the context
     *
     * @param subTask sub task called to filter the elemen
     * @return this task to chain actions (fluent API)
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

    /**
     * Traverse a relation indexed by {@code indexName} and retrieve specific node thanks to the {@code query}
     *
     * @param indexName index name of indexed relation
     * @param query     query to retrieve specific nodes
     * @return this task to chain actions (fluent API)
     */
    Task traverseIndex(String indexName, String query);

    /**
     * Traverse a relation indexed by {@code indexName}
     *
     * @param indexName index name of indexed relation
     * @return this task to chain actions (fluent API)
     */
    Task traverseIndexAll(String indexName);

    Task map(TaskFunctionMap mapFunction);

    Task flatMap(TaskFunctionFlatMap flatMapFunction);

    Task group(TaskFunctionGroup groupFunction);

    Task groupWhere(Task groupSubTask);

    /**
     * Iterate through a collection and call the sub task for each elements
     *
     * @param subTask sub task to call for each elements
     * @return this task to chain actions (fluent API)
     */
    Task foreach(Task subTask);

    /**
     * Same as {@link #foreachThen(Callback)} method, but all the subtask are called in parallel
     * There is thus as thread as element in the collection
     *
     * @param subTask sub task to call for each elements
     * @return this task to chain actions (fluent API)
     */
    Task foreachPar(Task subTask);

    /**
     * Iterate through a pre-loaded collection of object add apply the {@code action} on each element
     * If you want to access/modify the context, please use {@link #foreach(Task)} method
     *
     * @param action action to apply on each element
     * @param <T>    type of the element
     * @return this task to chain actions (fluent API)
     */
    <T> Task foreachThen(Callback<T> action);

    /**
     * Execute and wait a sub task, result of this sub task is immediately enqueue and available for next
     *
     * @param subTask that have to be executed
     * @return this task to chain actions (fluent API)
     */
    Task wait(Task subTask);

    /**
     * Execute a sub task if the condition is satisfied
     *
     * @param cond condition to check
     * @param then sub task to execute if the condition is satisfied
     * @return this task to chain actions (fluent API)
     */
    Task ifThen(TaskFunctionConditional cond, Task then);

    Task whileDo(TaskFunctionConditional cond, Task then);

    Task then(TaskAction action);

    Task thenAsync(TaskAction action);

    Task save();

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

    /**
     * Schedule and execute the current task program. However
     *
     * @param parentContext initial context, only in case of cascade execution of tasks, null otherwise
     * @param initialResult initial content if any, null otherwise
     * @param finalAction   last action the execution before the clean procedure. Warning this last action will be executed in asynchronous mode. Therefore, no objects of the task will be freed before the call the method next on the parameter context.
     */
    void executeThenAsync(final TaskContext parentContext, final Object initialResult, final TaskAction finalAction);


}
