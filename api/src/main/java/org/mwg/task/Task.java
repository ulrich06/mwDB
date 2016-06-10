package org.mwg.task;

import org.mwg.Callback;
import org.mwg.Node;
import org.mwg.Type;

public interface Task {

    /**
     * Method to set the task context to a particular world.
     *
     * @param world that hasField to be set into the task context and will be used for next sub tasks.
     * @return this task to chain actions (fluent API)
     */
    Task setWorld(long world);

    /**
     * Method to set the task context to a particular time.
     *
     * @param time that hasField to be set into the task context and will be used for next sub tasks
     * @return this task to chain actions (fluent API)
     */
    Task setTime(long time);

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

    Task setVar(String variableName, Object inputValue);

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
     * Retrieve any property given a precise name.
     * If the property is a relationship, it is traversed an related nodes are retrieved.
     *
     * @param name of property to
     * @return this task to chain actions (fluent API)
     */
    Task get(String name);

    /**
     * Traverse the specified relation if not empty, otherwise keep leaf nodes
     * If it is followed by {@link #asVar(String)} method, the element are stored in an array
     *
     * @param relationName relation to traverse if not empty
     * @return this task to chain actions (fluent API)
     */
    Task traverseOrKeep(String relationName);

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
    Task executeSubTask(Task subTask);

    /**
     * Execute a sub task if the condition is satisfied
     *
     * @param cond condition to check
     * @param then sub task to execute if the condition is satisfied
     * @return this task to chain actions (fluent API)
     */
    Task ifThen(TaskFunctionConditional cond, Task then);

    Task whileDo(TaskFunctionConditional cond, Task then);

    Task then(Action action);

    Task thenAsync(Action action);

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
    void executeThen(Action action);

    void executeWith(TaskContext initialContext);

    /**
     * Schedule and execute the current task program. However
     *
     * @param parentContext initial context, only in case of cascade execution of tasks, null otherwise
     * @param initialResult initial content if any, null otherwise
     * @param finalAction   last action the execution before the clean procedure. Warning this last action will be executed in asynchronous mode. Therefore, no objects of the task will be freed before the call the method next on the parameter context.
     */
    void executeThenAsync(final TaskContext parentContext, final Object initialResult, final Action finalAction);

    /**
     * Create a new node on the [world,time] of the context
     *
     * @return this task to chain actions (fluent API)
     */
    Task newNode();

    /**
     * Sets the value of an attribute of a node or an array of nodes
     * The node (or the array) should be init in the previous task
     *
     * @param propertyName      The name of the attribute. Must be unique per node.
     * @param variableNameToSet The name of the property to set, should be stored previously as a variable in task context.
     * @return this task to chain actions (fluent API)
     */
    Task set(String propertyName, String variableNameToSet);

    /**
     * Sets the value of an attribute of a node or an array of nodes
     * The node (or the array) should be init in the previous task
     *
     * @param propertyName      The name of the attribute. Must be unique per node.
     * @param propertyType      The type of the attribute. Must be one of {@link Type} int value.
     * @param variableNameToSet The name of the property to set, should be stored previously as a variable in task context.
     * @return this task to chain actions (fluent API)
     */
    Task setProperty(String propertyName, byte propertyType, String variableNameToSet);

    /**
     * Removes an attribute from a node or an array of nodes.
     * The node (or the array) should be init in the previous task
     *
     * @param propertyName The name of the attribute to remove.
     * @return this task to chain actions (fluent API)
     */
    Task removeProperty(String propertyName);

    /**
     * Adds a node to a relation of a node or of an array of nodes.
     *
     * @param relationName      The name of the relation.
     * @param variableNameToAdd The name of the property to add, should be stored previously as a variable in task context.
     * @return this task to chain actions (fluent API)
     */
    Task add(String relationName, String variableNameToAdd);

    /**
     * Removes a node from a relation of a node or of an array of nodes.
     *
     * @param relationName         The name of the relation.
     * @param variableNameToRemove The name of the property to add, should be stored previously as a variable in task context.
     * @return this task to chain actions (fluent API)
     */
    Task remove(String relationName, String variableNameToRemove);

    /**
     * Parse a string to build the current task. Syntax should follow: actionName(param).actionName2(param2)...
     * In case actionName en () are empty, default task is get(name).
     * Therefore the following: children.name should be read as get(children).get(name)
     *
     * @param flat string definition of the task
     * @return this task to chain actions (fluent API)
     */
    Task parse(String flat);

    /**
     * Build a named action, based on the task registry.
     * This allows to extend task API with your own DSL.
     *
     * @param name   designation of the task to add, should correspond to the name of the Task plugin registered.
     * @param params parameters of the newly created task
     * @return this task to chain actions (fluent API)
     */
    Task action(String name, String params);

    /**
     * Execute a math expression on all nodes given from previous step
     *
     * @param expression math expression to execute
     * @return this task to chain actions (fluent API)
     */
    Task math(String expression);

}
