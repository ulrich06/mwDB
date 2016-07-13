package org.mwg.task;

import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.Type;

import java.util.Map;

public interface Task {

    /**
     * Sets the task context to a particular world.
     *
     * @param template to be set into the task context and will be used for next sub tasks.
     * @return this task to chain actions (fluent API)
     */
    Task setWorld(String template);

    /**
     * Sets the task context to a particular time.
     *
     * @param template that hasField to be set into the task context and will be used for next sub tasks
     * @return this task to chain actions (fluent API)
     */
    Task setTime(String template);

    /**
     * Stores the current task result into a named variable
     *
     * @param variableName identifier of this result
     * @return this task to chain actions (fluent API)
     */
    Task asVar(String variableName);

    /**
     * Retrieves a previous task result and stack it for next sub tasks.
     *
     * @param variableName identifying a previous result
     * @return this task to chain actions (fluent API)
     */
    Task fromVar(String variableName);

    /**
     * Initializes a named variable into the task context.
     *
     * @param variableName the name of the variable
     * @param inputValue   the value of the variable
     * @return this task to chain actions (fluent API)
     */
    Task setVar(String variableName, Object inputValue);

    /**
     * Method to initialise a task with any object
     *
     * @param inputValue object used as source of a task
     * @return this task to chain actions (fluent API)
     */
    Task inject(Object inputValue);

    /**
     * Retrieves indexed nodes that matches the query
     *
     * @param indexName name of the index to use
     * @param query     query to filter nodes, such as name=FOO
     * @return this task to chain actions (fluent API)
     */
    Task fromIndex(String indexName, String query);

    /**
     * Retrieves all nodes from a named index
     *
     * @param indexName name of the index
     * @return this task to chain actions (fluent API)
     */
    Task fromIndexAll(String indexName);

    /**
     * Index the node (or the array of nodes) present in the result
     *
     * @param indexName index name
     * @param flatKeyAttributes node attributes used to index
     * @return this task to chain actions (fluent API)
     */
    Task indexNode(String indexName, String flatKeyAttributes);

    /**
     * Unindex the node (or the array of nodes) present in the result
     *
     * @param indexName index name
     * @param flatKeyAttributes node attributes used to index
     * @return this task to chain actions (fluent API)
     */
    Task unindexNode(String indexName, String flatKeyAttributes);

    /**
     * Filters the previous result to keep nodes which named attribute has a specific value
     *
     * @param name    the name of the attribute used to filter
     * @param pattern the value nodes must have for this attribute
     * @return this task to chain actions (fluent API)
     */
    Task selectWith(String name, String pattern);


    /**
     * Filters the previous result to keep nodes which named attribute do not have a given value
     *
     * @param name    the name of the attribute used to filter
     * @param pattern the value nodes must not have for this attribute
     * @return this task to chain actions (fluent API)
     */
    Task selectWithout(String name, String pattern);

    /**
     * Filters the previous result to get nodes that complies to the condition specified in {@code filterFunction}
     * If you want to access/modify the context, please use {@link #selectWhere(Task)}
     *
     * @param filterFunction condition that nodes have to respect
     * @return this task to chain actions (fluent API)
     */
    Task select(TaskFunctionSelect filterFunction);

    /**
     * IN CONSTRUCTION - Not final state
     *
     * @param filterFunction condition that objects have to respect
     */
    Task selectObject(TaskFunctionSelectObject filterFunction);

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
     * @param name of property to retrieve
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
     * Iterate through a collection and calls the sub task for each elements
     *
     * @param subTask sub task to call for each elements
     * @return this task to chain actions (fluent API)
     */
    Task foreach(Task subTask);

    /**
     * Same as {@link #foreach(Task)} method, but all the subtask are called in parallel
     * There is thus as thread as element in the collection
     *
     * @param subTask sub task to call for each elements
     * @return this task to chain actions (fluent API)
     */
    Task foreachPar(Task subTask);

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

    Task save();

    /**
     * Create a new node on the [world,time] of the context
     *
     * @return this task to chain actions (fluent API)
     */
    Task newNode();

    /**
     * Create a new typed node on the [world,time] of the context
     *
     * @return this task to chain actions (fluent API)
     */
    Task newTypedNode(String typeNode);

    /**
     * Sets the value of an attribute of a node or an array of nodes with a variable value
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
     * Jump the node , or the array of nodes, in the result to the given time
     *
     * @param time Time to jump for each nodes
     * @return this task to chain actions (fluent API)
     */
    Task jump(String time);

    /**
     * Parse a string to build the current task. Syntax is as follow: actionName(param).actionName2(param2)...
     * In case actionName() are empty, default task is get(name).
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

    Task split(String splitPattern);

    Task lookup(String world, String time, String id);

    /**
     * Execute a math expression on all nodes given from previous step
     *
     * @param expression math expression to execute
     * @return this task to chain actions (fluent API)
     */
    Task math(String expression);

    Task repeat(int repetition, Task subTask);

    Task repeatPar(int repetition, Task subTask);

    Task print(String name);

    void execute(final Graph graph, final Callback<Object> result);

    void executeWith(final Graph graph, final Map<String, Object> variables, Object initialResult, final boolean isVerbose, final Callback<Object> result);

    void executeFrom(final TaskContext parent, final Object initialResult, final Callback<Object> result);

    void executeFromPar(final TaskContext parent, final Object initialResult, final Callback<Object> result);

}
