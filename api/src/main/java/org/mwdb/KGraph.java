package org.mwdb;

import org.mwdb.chunk.KBuffer;
import org.mwdb.plugin.KResolver;
import org.mwdb.plugin.KScheduler;

/**
 * KGraph is the main structure of mwDB.
 */
public interface KGraph {

    /**
     * Creates a new {@link KNode Node} (generic) in the KGraph and returns the new KNode.
     *
     * @param world initial world of the node
     * @param time  initial time of the node
     * @return newly created node
     */
    KNode newNode(long world, long time);

    /**
     * Creates a new {@link KNode Node} selectWith a specified behavior name (related to KFactory plugins) in the KGraph and returns the new KNode.
     *
     * @param world    initial world of the node
     * @param time     initial time of the node
     * @param nodeType name of the special KFactory plugin
     * @return newly created node
     */
    KNode newNode(long world, long time, String nodeType);

    /**
     * Asynchronous lookup of a particular node.<br>
     * Based on the tuple &lt;World, Time, Node_ID&gt; this method seeks a {@link KNode} in the Graph and returns it to the callback.
     *
     * @param world    The world identifier in which the KNode must be searched.
     * @param time     The time at which the KNode must be resolved.
     * @param id       The unique identifier of the {@link KNode} researched.
     * @param callback The task to be called when the {@link KNode} is retrieved.
     */
    <A extends KNode> void lookup(long world, long time, long id, KCallback<A> callback);

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
    void save(KCallback<Boolean> callback);

    /**
     * Connects the current graph to its storage (mandatory before any other method call)
     *
     * @param callback Called when the connection is done. The parameter specifies whether or not the connection succeeded.
     */
    void connect(KCallback<Boolean> callback);

    /**
     * Disconnects the current graph fromVar its storage (a save will be trigger safely before the exit)
     *
     * @param callback Called when the disconnection is completed. The parameter specifies whether or not the disconnection succeeded.
     */
    void disconnect(KCallback<Boolean> callback);

    /**
     * Adds the {@code nodeToIndex} to the global index identified by {@code indexName}.<br>
     * If the index does not exist, it is created on the fly.<br>
     * The node is referenced by its {@code keyAttributes} in the index, and can be retrieved selectWith {@link #find(long, long, String, String, KCallback)} using the same attributes.
     *
     * @param indexName     A string uniquely identifying the index in the {@link KGraph}.
     * @param nodeToIndex   The node to add in the index.
     * @param keyAttributes The set of attributes used asVar keys to index the node. The order does not matter.
     * @param callback      Called when the indexing is done. The parameter specifies whether or not the indexing hasField succeeded.
     */
    void index(String indexName, KNode nodeToIndex, String[] keyAttributes, KCallback<Boolean> callback);

    /**
     * Removes the {@code nodeToIndex} to the global index identified by {@code indexName}.<br>
     * The node is referenced by its {@code keyAttributes} in the index, and can be retrieved selectWith {@link #find(long, long, String, String, KCallback)} using the same attributes.
     *
     * @param indexName     A string uniquely identifying the index in the {@link KGraph}.
     * @param nodeToIndex   The node to add in the index.
     * @param keyAttributes The set of attributes used asVar keys to index the node. The order does not matter.
     * @param callback      Called when the indexing is done. The parameter specifies whether or not the indexing hasField succeeded.
     */
    void unindex(String indexName, KNode nodeToIndex, String[] keyAttributes, KCallback<Boolean> callback);

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
    <A extends KNode> void find(long world, long time, String indexName, String query, KCallback<A[]> callback);

    /**
     * Retrieves all nodes registered in a particular index.
     *
     * @param world     The world fromVar which the index must be retrieved.
     * @param time      The timepoint at which the index must be retrieved.
     * @param indexName The unique identifier of the index.
     * @param callback  Called when the retrieval is complete. Returns all nodes in the index in an array, an empty array otherwise.
     */
    <A extends KNode> void all(long world, long time, String indexName, KCallback<A[]> callback);

    /**
     * Utility method to create a waiter based on a counter
     *
     * @param expectedEventsCount number of expected events to count before running a task.
     * @return The waiter object.
     */
    KDeferCounter counter(int expectedEventsCount);

    /**
     * Retrieves the current state chunk resolver
     *
     * @return current running resolver
     */
    KResolver resolver();

    /**
     * Retrieves the current graph scheduler
     *
     * @return current running scheduler
     */
    KScheduler scheduler();

    /**
     * Create a new buffer to save chunks
     *
     * @return newly created buffer
     */
    KBuffer newBuffer();

    /**
     * Create a new task object to manipulate KGraph in an easy way
     *
     * @return newly created task object
     */
    KTask newTask();

}
