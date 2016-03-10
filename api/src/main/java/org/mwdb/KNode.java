package org.mwdb;


/**
 * KNode is the base element contained in the {@link KGraph}.
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
     * Provides the identifier for this node in the graph.
     * Thsi identifier is constant over timePoints and worlds.
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
     * ALlows to know the type of an attribute. The returned value is one of {@link KType}.
     *
     * @param attributeName The name of the attribute for which the type is asked.
     * @return The type of the attribute inform of an int belonging to {@link KType}.
     */
    int attType(String attributeName);

    /**
     * Sets the value of an attribute of this node, for its current world and time.
     * This method has to be used for primitive types.
     *
     * @param attributeName  The name of the attribute. Must be unique per node.
     * @param attributeType  The type of the attribute. Must be one of {@link KType} int value.
     * @param attributeValue The value of the attribute. Must be consistent with the attributeType.
     */
    void attSet(String attributeName, short attributeType, Object attributeValue);

    /**
     * Gets or creates atomically a complex type (such as Maps).
     * It returns a mutable Map.
     *
     * @param attributeName The name of the Map to create. Must be unique per node.
     * @param attributeType The type of the attribute. Must be one of {@link KType} int value.
     * @return A Map instance that can be altered at the current world and time.
     */
    Object attMap(String attributeName, short attributeType);

    /**
     * Removes an attribute from the node.
     *
     * @param attributeName The name of the attribute to remove.
     */
    void attRemove(String attributeName);

    /**
     * Retrieves asynchronously the nodes contained in a relation.
     * @param relationName The name of the relation to retrieve.
     * @param callback Callback to be called when the nodes of the relationship have been connected.
     */
    void rel(String relationName, KCallback<KNode[]> callback);

    /**
     * Retrieves asynchronously the nodes contained in a relation.
     * @param relationName The name of the relation to retrieve.
     * @return An array of node ids contained in the relation.
     */
    long[] relValues(String relationName);

    /**
     * Adds a node to a relation.
     * If the relationship doesn't exist, it is created on the fly.
     * The relation name must be unique in the node.
     *
     * @param relationName The name of the relation in which to add the node.
     * @param relatedNode The node to insert in the relation.
     */
    void relAdd(String relationName, KNode relatedNode);

    /**
     * Removes a node from a relation.
     * @param relationName The name of the relation.
     * @param relatedNode The node to remove from the relation.
     */
    void relRemove(String relationName, KNode relatedNode);

    /**
     * Creates or compliments an index of nodes.
     * Indexes are special relationships for quick access to referred nodes based on some of their attributes values.
     * Index names must be unique within a given node.
     *
     * @param indexName     The name of the index (should be unique per node).
     * @param nodeToIndex   The new node to index.
     * @param keyAttributes The list of attribute names to be used as keys for indexing (order does not matter)
     * @param callback      Called when the index has been created/updated. The boolean value specifies the success of the operation.
     */
    void index(String indexName, KNode nodeToIndex, String[] keyAttributes, KCallback<Boolean> callback);

    /**
     * Retrieves a node from an index that satisfies a query.
     * The query is composed by &lt;key, value&gt; tuples, separated by commas.
     *
     * @param indexName name of the index (should be unique per node)
     * @param query     textual query of the form (attName=val,attName2=val2...) such as: name=john,age=30
     * @param callback  result closure
     */
    void find(String indexName, String query, KCallback<KNode> callback);

    /**
     * Retrieve all indexed nodes by a particular index
     *
     * @param indexName name of the index (should be unique per node)
     * @param callback  result closure
     */
    void all(String indexName, KCallback<KNode[]> callback);

    /**
     * Compute the time dephasing of this node (difference between last modification and desired time).
     *
     * @return time distance with last recorded state chunk for this node.
     */
    long timeDephasing();

    /**
     * mwDB nodes can potentially have a dePhasing (difference between last resolved state time and the desired timePoints).
     * A call to this method will force this node to create a precise timePoint for it's time, allowing later to do some modification.
     * In a nutshell, this method will clone the previous state to the exact time of this node.
     */
    void forcePhase();

    /**
     * Retrieve all timePoints from the timeLine of this node where modifications have been recorded.
     * In case of a many world graph, this method will jump over the world hierarchy in order to collect all available timepoints
     * In case of an unbounded search, please use Constants.BEGINNING_OF_TIME and Constants.END_OF_TIME as bounds.
     *
     * @param beginningOfSearch (inclusive) lower bounds for the result timePoints set.
     * @param endOfSearch       (inclusive) upper bounds for the result timePoints set.
     * @param callback          result closure
     */
    void timepoints(long beginningOfSearch, long endOfSearch, KCallback<long[]> callback);

    /**
     * Memory Management section
     */

    /**
     * Inform mwDB memory manager that this node object will not be used anymore.
     * Warning this method should be the last one called on this node.
     * If the same graph node as to be explore, a new lookup call is mandatory.
     */
    void free();

}
