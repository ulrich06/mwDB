package org.mwdb;

import org.mwdb.chunk.KBuffer;

/**
 * KGraph is the main structure of mwDB.
 */
public interface KGraph {

    /**
     * Creates a new {@link KNode Node} in the KGraph and returns the new KNode.
     *
     * @param world initial world of the node
     * @param time  initial time of the node
     * @return newly created node
     */
    KNode newNode(long world, long time);

    /**
     * Asynchronous lookup of a particular node.<br>
     * Based on the tuple &lt;World, Time, Node_ID&gt; this method seeks a {@link KNode} in the Graph and returns it to the callback.
     *
     * @param world    The world identifier in which the KNode must be searched.
     * @param time     The time at which the KNode must be resolved.
     * @param id       The unique identifier of the {@link KNode} researched.
     * @param callback The task to be called when the {@link KNode} is retrieved.
     */
    void lookup(long world, long time, long id, KCallback<KNode> callback);

    /**
     * Creates a spin-off world from the world given as parameter.<br>
     * The forked world can then be altered independently of its parent.<br>
     * Every modification in the parent world will nevertheless be inherited.
     *
     * @param world origin world id
     * @return newly created child world id (to be used later in lookup method for instance)
     */
    long diverge(long world);

    /**
     * Triggers a save task for the current graph.<br>
     * This method synchronizes the storage with the current RAM memory.
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
     * Disconnects the current graph from its storage (a save will be trigger safely before the exit)
     *
     * @param callback Called when the disconnection is completed. The parameter specifies whether or not the disconnection succeeded.
     */
    void disconnect(KCallback<Boolean> callback);

    /**
     * Adds the {@code nodeToIndex} to the global index identified by {@code indexName}.<br>
     * If the index does not exist, it is created on the fly.<br>
     * The node is referenced by its {@code keyAttributes} in the index, and can be retrieved with {@link #find(long, long, String, String, KCallback)} using the same attributes.
     *
     * @param indexName     A string uniquely identifying the index in the {@link KGraph}.
     * @param nodeToIndex   The node to add in the index.
     * @param keyAttributes The set of attributes used as keys to index the node. The order does not matter.
     * @param callback      Called when the indexing is done. The parameter specifies whether or not the indexing has succeeded.
     */
    void index(String indexName, KNode nodeToIndex, String[] keyAttributes, KCallback<Boolean> callback);

    /**
     * Retrieves the node from an index that satisfies the query.<br>
     * The query must be defined using at least sub-set attributes used for the indexing, or all of them.<br>
     * The form of the query is a list of &lt;key, value&gt; tuples (i.e.: "&lt;attName&gt;=&lt;val&gt;, &lt;attName2&gt;=&lt;val2&gt;,...").<br>
     * e.g: "name=john,age=30"
     *
     * @param world     The world id in which the search must be performed.
     * @param time      The timepoint at which the search must be performed.
     * @param indexName The name of the index in which to search.
     * @param query     The query the node must satisfy.
     * @param callback  Called when the search is finished. The requested node is given in parameter, null otherwise.
     */
    void find(long world, long time, String indexName, String query, KCallback<KNode> callback);

    /**
     * Retrieves all nodes registered in a particular index.
     *
     * @param world     The world from which the index must be retrieved.
     * @param time      The timepoint at which the index must be retrieved.
     * @param indexName The unique identifier of the index.
     * @param callback  Called when the retrieval is complete. Returns all nodes in the index in an array, an empty array otherwise.
     */
    void all(long world, long time, String indexName, KCallback<KNode[]> callback);

    /**
     * Utility method to create a waiter based on a counter
     *
     * @param expectedEventsCount number of expected events to count before running a task.
     * @return The waiter object.
     */
    KDeferCounter counter(int expectedEventsCount);

    KBuffer newBuffer();

}
