package org.mwdb;

public interface KGraph {

    /**
     * Create a new node in the Many world Graph
     *
     * @param world initial world of the node
     * @param time  initial time of the node
     * @return newly created node
     */
    KNode createNode(long world, long time);

    /**
     * Asynchronous lookup for a particular node.
     * Based on the tuple World/Time/Node_ID this method will prepare a KNode object and return it as ready in the callback.
     *
     * @param world    current reading world
     * @param time     current reading timePoint
     * @param id       current nodeId
     * @param callback closure result
     */
    void lookup(long world, long time, long id, KCallback<KNode> callback);

    /**
     * Create a spin-off world from the world passed as parameter.
     * This forked world will allow independant modification.
     * However, every modification from the parent will be inherited.
     *
     * @param world origin world id
     * @return newly created child world id (to be used later in lookup method for instance)
     */
    long diverge(long world);

    /**
     * Trigger a save task for the current graph.
     * This method synchronize RAM memory with current configured storage driver
     *
     * @param callback result closure
     */
    void save(KCallback callback);

    /**
     * Connect the current graph (mandatory before any other method call)
     *
     * @param callback result closure
     */
    void connect(KCallback callback);

    /**
     * Disconnect the current graph (a save will be trigger safely before the exit)
     *
     * @param callback result closure
     */
    void disconnect(KCallback callback);

    /**
     * Index a node by the current one.
     * Indexes are special relationships for quick access to referred nodes based on some of their attributes values.
     *
     * @param indexName     name of the index (should be unique per node)
     * @param toIndexNode   node to index
     * @param keyAttributes list of key names to be part of the index (order does not matter)
     * @param callback      result closure
     */
    void index(String indexName, KNode toIndexNode, String[] keyAttributes, KCallback callback);

    /**
     * Retrieve a node in a particular index based on a query (containing key,value tuples)
     *
     * @param world     current reading world
     * @param time      current reading timePoint
     * @param indexName name of the index (should be unique per node)
     * @param query     textual query of the form (attName=val,attName2=val2...) such as: name=john,age=30
     * @param callback  result closure
     */
    void find(long world, long time, String indexName, String query, KCallback<KNode> callback);

    /**
     * Retrieve all indexed nodes by a particular index
     *
     * @param world     current reading world
     * @param time      current reading timePoint
     * @param indexName name of the index (should be unique per node)
     * @param callback  result closure
     */
    void all(long world, long time, String indexName, KCallback<KNode[]> callback);

    /**
     * Utility method to create a waiter based on a counter
     *
     * @param expectedCountCalls number of expected call to count method before continuing.
     * @return waiter object.
     */
    KDeferCounter counter(int expectedCountCalls);

}
