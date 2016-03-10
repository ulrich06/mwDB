package org.mwdb;

public interface KNode {

    /**
     * Current world where this node will read its state (attributes, relationships, indexes...)
     *
     * @return current world id
     */
    long world();

    /**
     * Current time where this node will read its state (attributes, relationships, indexes...)
     *
     * @return current time id (aka current timePoint)
     */
    long time();

    /**
     * Current unique node identifier (consistent over the various timePoints and worlds)
     *
     * @return current node id
     */
    long id();

    /**
     * Retrieve the content of an attribute in a node (similarly to an hashMap storage)
     *
     * @param attributeName name of the attribute (should be unique per node)
     * @return current content for this attribute for the current world and time (typed by one of the KType)
     */
    Object att(String attributeName);

    /**
     * Retrieve the current type of this attribute.
     * Should be in the list of KType definition.
     *
     * @param attributeName name of the attribute (should be unique per node)
     * @return current type (coded as a int) for the content of this attribute.
     */
    int attType(String attributeName);

    /**
     * Fill a value for an attribute of this node (for this world and time)
     *
     * @param attributeName  name of the attribute (should be unique per node)
     * @param attributeType  type of the attribute (should be one of the KType definition, a int value)
     * @param attributeValue next payload of the attribute (should be consistent with the attributeType)
     */
    void attSet(String attributeName, int attributeType, Object attributeValue);

    /**
     * Get or create the content of an attribute.
     * This method atomically get or create the value according to the type passed as parameter.
     * In particular this method is mandatory to use complex types such as maps.
     *
     * @param attributeName name of the attribute (should be unique per node)
     * @param attributeType type of the attribute (should be one of the KType definition, a int value)
     * @return current content for this attribute for the current world and time (typed by one of the KType)
     */
    Object attMap(String attributeName, int attributeType);

    /**
     * Remove the named attribute from this node
     *
     * @param attributeName name of the attribute (should be unique per node)
     */
    void attRemove(String attributeName);

    /**
     * Retrieve asynchronously related nodes based on the relation name.
     * @param relationName name of the relation (should be unique per node)
     * @param callback result closure
     */
    void ref(String relationName, KCallback<KNode[]> callback);

    /**
     * Retrieve synchronously values of a relation
     * @param relationName name of the relation (should be unique per node)
     * @return array of node ids contained in the relation.
     */
    long[] refValues(String relationName);

    /**
     * Add a node to a relation
     * @param relationName name of the relation (should be unique per node)
     * @param relatedNode node to insert in the relation
     */
    void refAdd(String relationName, KNode relatedNode);

    /**
     * Remove a node from a relation
     * @param relationName name of the relation (should be unique per node)
     * @param relatedNode node to remove in the relation
     */
    void refRemove(String relationName, KNode relatedNode);

    /**
     * Index a node by the current one.
     * Indexes are special relationships for quick access to referred nodes based on some of their attributes values.
     *
     * @param indexName     name of the index (should be unique per node)
     * @param toIndexNode   node to index
     * @param keyAttributes list of key names to be part of the index (order does not matter)
     * @param callback      result closure
     */
    void index(String indexName, KNode toIndexNode, String[] keyAttributes, KCallback<Boolean> callback);

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
