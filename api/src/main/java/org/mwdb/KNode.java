package org.mwdb;

/**
 * KNode is the base element contained in the {@link KGraph}.<br>
 * They belong to a world and time, have attributes, relationships, and indexes.
 */
public interface KNode {

    /**
     * The world this node belongs to.
     *
     * @return World identifier
     */
    long world();

    /**
     * Provides the timepoint of the node.
     *
     * @return Timestamp value
     */
    long time();

    /**
     * Provides the identifier for this node in the graph.<br>
     * This identifier is constant over timePoints and worlds.
     *
     * @return the node id.
     */
    long id();

    /**
     * Returns the value of an attribute of the node.
     *
     * @param attributeName The name of the attribute to be read.
     * @return The value of the required attribute in this node for the current timepoint and world.
     * The type of the returned object (i.e.: of the attribute) is given by {@link #attType(String)}
     * (typed by one of the KType)
     */
    Object att(String attributeName);

    /**
     * Allows to know the type of an attribute. The returned value is one of {@link KType}.
     *
     * @param attributeName The name of the attribute for which the type is asked.
     * @return The type of the attribute inform of an int belonging to {@link KType}.
     */
    byte attType(String attributeName);

    /**
     * Sets the value of an attribute of this node, for its current world and time.<br>
     * This method has to be used for primitive types.
     *
     * @param attributeName  The name of the attribute. Must be unique per node.
     * @param attributeType  The type of the attribute. Must be one of {@link KType} int value.
     * @param attributeValue The value of the attribute. Must be consistent with the attributeType.
     */
    void attSet(String attributeName, byte attributeType, Object attributeValue);

    /**
     * Gets or creates atomically a complex type (such as Maps).<br>
     * It returns a mutable Map.
     *
     * @param attributeName The name of the Map to create. Must be unique per node.
     * @param attributeType The type of the attribute. Must be one of {@link KType} int value.
     * @return A Map instance that can be altered at the current world and time.
     */
    Object attMap(String attributeName, byte attributeType);

    /**
     * Removes an attribute from the node.
     *
     * @param attributeName The name of the attribute to remove.
     */
    void attRemove(String attributeName);

    /**
     * Retrieves asynchronously the nodes contained in a relation.
     *
     * @param relationName The name of the relation to retrieve.
     * @param callback     Callback to be called when the nodes of the relationship have been connected.
     */
    void rel(String relationName, KCallback<KNode[]> callback);

    /**
     * Retrieves synchronously the nodes contained in a relation.
     *
     * @param relationName The name of the relation to retrieve.
     * @return An array of node ids contained in the relation.
     */
    long[] relValues(String relationName);

    /**
     * Adds a node to a relation.<br>
     * If the relationship doesn't exist, it is created on the fly.<br>
     * The relation name must be unique in the node.
     *
     * @param relationName The name of the relation in which to add the node.
     * @param relatedNode  The node to insert in the relation.
     */
    void relAdd(String relationName, KNode relatedNode);

    /**
     * Removes a node from a relation.
     *
     * @param relationName The name of the relation.
     * @param relatedNode  The node to remove from the relation.
     */
    void relRemove(String relationName, KNode relatedNode);

    /**
     * Creates or compliments an index of nodes.<br>
     * Indexes are special relationships for quick access to referred nodes based on some of their attributes values.<br>
     * Index names must be unique within a given node.
     *
     * @param indexName     The name of the index (should be unique per node).
     * @param nodeToIndex   The new node to index.
     * @param keyAttributes The list of attribute names to be used as keys for indexing (order does not matter)
     * @param callback      Called when the index has been created/updated. The boolean value specifies the success of the operation.
     */
    void index(String indexName, KNode nodeToIndex, String[] keyAttributes, KCallback<Boolean> callback);

    /**
     * Removes an element from an index of nodes.<br>
     * Indexes are special relationships for quick access to referred nodes based on some of their attributes values.<br>
     * Index names must be unique within a given node.
     *
     * @param indexName     The name of the index (should be unique per node).
     * @param nodeToIndex   The new node to index.
     * @param keyAttributes The list of attribute names to be used as keys for indexing (order does not matter)
     * @param callback      Called when the index has been created/updated. The boolean value specifies the success of the operation.
     */
    void unindex(String indexName, KNode nodeToIndex, String[] keyAttributes, KCallback<Boolean> callback);

    /**
     * Retrieves nodes from an index that satisfies a query.<br>
     * The query is composed by &lt;key, value&gt; tuples, separated by commas.
     *
     * @param indexName The name of the index (should be unique per node)
     * @param query     The query (e.g.: "firstname=john,lastname=doe,age=30"
     * @param callback  Called when the task is fully processed. The parameter is the requested nodes, empty array otherwise.
     */
    void find(String indexName, String query, KCallback<KNode[]> callback);

    /**
     * Retrieves all nodes in a particular index
     *
     * @param indexName The name of the index
     * @param callback  Called whe the collection is complete. Gives the list of contained nodes in parameter.
     */
    void all(String indexName, KCallback<KNode[]> callback);

    /**
     * Compute the time dephasing of this node, i.e. the difference between last modification and current node timepoint.
     *
     * @return The amount of time between the current time of the node and the last recorded state chunk time.
     */
    long timeDephasing();

    /**
     * Forces the creation of a new timePoint of a node for its time.<br>
     * Clones the previous state to the exact time of this node.<br>
     * This cancels the dephasing between the current timepoint of the node and the last record timepoint.
     */
    void forcePhase();

    /**
     * Retrieves all timePoints from the timeLine of this node when alterations occurred.<br>
     * This method also jumps over the world hierarchy to collect all available timepoints.<br>
     * To unbound the search, please use {@link KConstants#BEGINNING_OF_TIME} and {@link KConstants#END_OF_TIME} as bounds.
     *
     * @param beginningOfSearch (inclusive) earliest bound for the search.
     * @param endOfSearch       (inclusive) latest bound for the search.
     * @param callback          Called when the search is finished. Provides an array containing all the timepoints required.
     */
    void timepoints(long beginningOfSearch, long endOfSearch, KCallback<long[]> callback);

    /**
     * Informs mwDB memory manager that this node object can be freed from the memory.<br>
     * <b>Warning: this MUST be the last method called on this node.</b><br>
     * To work with the node afterwards, a new lookup is mandatory.
     */
    void free();

    /**
     * Return the graph that have created this node.
     *
     * @return the graph this node belongs to
     */
    KGraph graph();

}
