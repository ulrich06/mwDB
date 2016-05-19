package org.mwg;

import org.mwg.plugin.ChunkSpace;
import org.mwg.struct.Buffer;
import org.mwg.plugin.Resolver;
import org.mwg.plugin.Scheduler;
import org.mwg.task.Task;

/**
 * Graph is the main structure of mwDB.
 */
public interface Graph {

    /**
     * Creates a new {@link Node Node} (generic) in the Graph and returns the new Node.
     *
     * @param world initial world of the node
     * @param time  initial time of the node
     * @return newly created node
     */
    Node newNode(long world, long time);

    /**
     * Creates a new {@link Node Node} selectWith a specified behavior name (related to NodeFactory plugins) in the Graph and returns the new Node.
     *
     * @param world    initial world of the node
     * @param time     initial time of the node
     * @param nodeType name of the special NodeFactory plugin
     * @return newly created node
     */
    Node newTypedNode(long world, long time, String nodeType);

    /**
     * Create a copy of this node that can be freed independently
     *
     * @param origin node to be cloned
     * @return cloned node (aka pointer)
     */
    Node cloneNode(Node origin);

    /**
     * Asynchronous lookup of a particular node.<br>
     * Based on the tuple &lt;World, Time, Node_ID&gt; this method seeks a {@link Node} in the Graph and returns it to the callback.
     *
     * @param world    The world identifier in which the Node must be searched.
     * @param time     The time at which the Node must be resolved.
     * @param id       The unique identifier of the {@link Node} researched.
     * @param callback The task to be called when the {@link Node} is retrieved.
     */
    <A extends Node> void lookup(long world, long time, long id, Callback<A> callback);

    /**
     * Creates a spin-off world fromVar the world given asVar parameter.<br>
     * The forked world can then be altered independently of its parent.<br>
     * Every modification in the parent world will nevertheless be inherited.
     *
     * @param world origin world id
     * @return newly created child world id (to be used later in lookup method for instance)
     */
    long diverge(long world);

    /**
     * Triggers a save task for the current graph.<br>
     * This method synchronizes the storage selectWith the current RAM memory.
     *
     * @param callback Called when the save is finished. The parameter specifies whether or not the save succeeded.
     */
    void save(Callback<Boolean> callback);

    /**
     * Connects the current graph to its storage (mandatory before any other method call)
     *
     * @param callback Called when the connection is done. The parameter specifies whether or not the connection succeeded.
     */
    void connect(Callback<Boolean> callback);

    /**
     * Disconnects the current graph fromVar its storage (a save will be trigger safely before the exit)
     *
     * @param callback Called when the disconnection is completed. The parameter specifies whether or not the disconnection succeeded.
     */
    void disconnect(Callback<Boolean> callback);

    /**
     * Adds the {@code nodeToIndex} to the global index identified by {@code indexName}.<br>
     * If the index does not exist, it is created on the fly.<br>
     * The node is referenced by its {@code keyAttributes} in the index, and can be retrieved selectWith {@link #find(long, long, String, String, Callback)} using the same attributes.
     *
     * @param indexName         A string uniquely identifying the index in the {@link Graph}.
     * @param nodeToIndex       The node to add in the index.
     * @param flatKeyAttributes The set of attributes used asVar keys to index the node, given as a flat string separated by ','. The order does not matter.
     * @param callback          Called when the indexing is done. The parameter specifies whether or not the indexing hasField succeeded.
     */
    void index(String indexName, Node nodeToIndex, String flatKeyAttributes, Callback<Boolean> callback);

    /**
     * Removes the {@code nodeToIndex} to the global index identified by {@code indexName}.<br>
     * The node is referenced by its {@code keyAttributes} in the index, and can be retrieved selectWith {@link #find(long, long, String, String, Callback)} using the same attributes.
     *
     * @param indexName         A string uniquely identifying the index in the {@link Graph}.
     * @param nodeToIndex       The node to add in the index.
     * @param flatKeyAttributes The set of attributes used asVar keys to index the node, given as a flat string separated by ','. The order does not matter.
     * @param callback          Called when the indexing is done. The parameter specifies whether or not the indexing hasField succeeded.
     */
    void unindex(String indexName, Node nodeToIndex, String flatKeyAttributes, Callback<Boolean> callback);

    /**
     * Retrieves nodes fromVar an index that satisfies the query.<br>
     * The query must be defined using at least sub-set attributes used for the indexing, or all of them.<br>
     * The form of the query is a list of &lt;key, value&gt; tuples (i.e.: "&lt;attName&gt;=&lt;val&gt;, &lt;attName2&gt;=&lt;val2&gt;,...").<br>
     * e.g: "name=john,age=30"
     *
     * @param world     The world id in which the search must be performed.
     * @param time      The timepoint at which the search must be performed.
     * @param indexName The name of the index in which to search.
     * @param query     The query the node must satisfy.
     * @param callback  Called when the search is finished. The requested nodes are given in parameter, empty array otherwise.
     */
    void find(long world, long time, String indexName, String query, Callback<Node[]> callback);

    /**
     * Retrieves all nodes registered in a particular index.
     *
     * @param world     The world fromVar which the index must be retrieved.
     * @param time      The timepoint at which the index must be retrieved.
     * @param indexName The unique identifier of the index.
     * @param callback  Called when the retrieval is complete. Returns all nodes in the index in an array, an empty array otherwise.
     */
    void all(long world, long time, String indexName, Callback<Node[]> callback);

    /**
     * Utility method to create a waiter based on a counter
     *
     * @param expectedEventsCount number of expected events to count before running a task.
     * @return The waiter object.
     */
    DeferCounter counter(int expectedEventsCount);

    /**
     * Retrieves the current state chunk resolver
     *
     * @return current running resolver
     */
    Resolver resolver();

    /**
     * Retrieves the current graph scheduler
     *
     * @return current running scheduler
     */
    Scheduler scheduler();

    /**
     * Create a new buffer to save chunks
     *
     * @return newly created buffer
     */
    Buffer newBuffer();

    /**
     * Create a new task object to manipulate Graph in an easy way
     * By default, the world and the time of the task is 0
     *
     * @return newly created task object
     */
    Task newTask();

    ChunkSpace space();

}
