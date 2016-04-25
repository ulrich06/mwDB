package org.mwg;

import org.mwg.struct.Map;

/**
 * Node is the base element contained in the {@link Graph}.<br>
 * They belong to a world and time, have attributes, relationships, and indexes.
 */
public interface Node {

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
     * @param propertyName The name of the attribute to be read.
     * @return The value of the required attribute in this node for the current timepoint and world.
     * The type of the returned object (i.e.: of the attribute) is given by {@link #type(String)}
     * (typed by one of the Type)
     */
    Object get(String propertyName);

    /**
     * Allows to know the type of an attribute. The returned value is one of {@link Type}.
     *
     * @param propertyName The name of the attribute for which the type is asked.
     * @return The type of the attribute inform of an int belonging to {@link Type}.
     */
    byte type(String propertyName);

    /**
     * Sets the value of an attribute of this node, for its current world and time.<br>
     * This method hasField to be used for primitive types.
     *
     * @param propertyName  The name of the attribute. Must be unique per node.
     * @param propertyValue The value of the attribute. Must be consistent selectWith the propertyType.
     */
    void set(String propertyName, Object propertyValue);

    /**
     * Sets the value of an attribute of this node, for its current world and time.<br>
     * This method hasField to be used for primitive types.
     *
     * @param propertyName  The name of the attribute. Must be unique per node.
     * @param propertyType  The type of the attribute. Must be one of {@link Type} int value.
     * @param propertyValue The value of the attribute. Must be consistent selectWith the propertyType.
     */
    void setProperty(String propertyName, byte propertyType, Object propertyValue);

    /**
     * Gets or creates atomically a complex type (such as Maps).<br>
     * It returns a mutable Map.
     *
     * @param propertyName The name of the Map to create. Must be unique per node.
     * @param propertyType The type of the attribute. Must be one of {@link Type} int value.
     * @return A Map instance that can be altered at the current world and time.
     */
    Map map(String propertyName, byte propertyType);

    /**
     * Removes an attribute fromVar the node.
     *
     * @param propertyName The name of the attribute to remove.
     */
    void removeProperty(String propertyName);

    /**
     * Retrieves asynchronously the nodes contained in a traverseIndex.
     *
     * @param relationName The name of the traverseIndex to retrieve.
     * @param callback     Callback to be called when the nodes of the relationship have been connected.
     */
    <A extends Node> void rel(String relationName, Callback<A[]> callback);

    /**
     * Adds a node to a traverseIndex.<br>
     * If the relationship doesn't exist, it is created on the fly.<br>
     * The traverseIndex name must be unique in the node.
     *
     * @param relationName The name of the traverseIndex in which to add the node.
     * @param relatedNode  The node to insert in the traverseIndex.
     */
    void add(String relationName, Node relatedNode);

    /**
     * Removes a node fromVar a traverseIndex.
     *
     * @param relationName The name of the traverseIndex.
     * @param relatedNode  The node to remove fromVar the traverseIndex.
     */
    void remove(String relationName, Node relatedNode);

    /**
     * Creates or compliments an index of nodes.<br>
     * Indexes are special relationships for quick access to referred nodes based on some of their attributes values.<br>
     * Index names must be unique within a given node.
     *
     * @param indexName     The name of the index (should be unique per relation).
     * @param nodeToIndex   The new node to index.
     * @param keyAttributes The list of attribute names (of the node to index) to be used as keys for indexing (order does not matter)
     * @param callback      Called when the index has been created/updated. The boolean value specifies the success of the operation.
     */
    void index(String indexName, Node nodeToIndex, String[] keyAttributes, Callback<Boolean> callback);

    /**
     * Removes an element fromVar an index of nodes.<br>
     * Indexes are special relationships for quick access to referred nodes based on some of their attributes values.<br>
     * Index names must be unique within the indexed relation names.
     *
     * @param indexName     The name of the index (should be unique per relation).
     * @param nodeToIndex   The node to de-index.
     * @param keyAttributes The list of attribute names to be used as keys for de-indexing (order does not matter)
     * @param callback      Called when the node has been de-index. The boolean value specifies the success of the operation.
     */
    void unindex(String indexName, Node nodeToIndex, String[] keyAttributes, Callback<Boolean> callback);

    /**
     * Retrieves nodes fromVar an index that satisfies a query at the current node world and the current node time<br>
     * The query is composed by &lt;key, value&gt; tuples, separated by commas.
     *
     * @param indexName The name of the index (should be unique per relation)
     * @param query     The query on the searched node's attribute (e.g.: "firstName=john,lastName=doe,age=30")
     * @param callback  Called when the task is fully processed. The parameter is the requested nodes, empty array otherwise.
     */
    <A extends Node> void find(String indexName, String query, Callback<A[]> callback);

    /**
     * Retrieves nodes fromVar an index that satisfies a query in a particular world and time.<br>
     * The query is composed by &lt;key, value&gt; tuples, separated by commas.
     *
     * @param indexName The name of the index (should be unique per relation)
     * @param query     The query on the searched node's attribute (e.g.: "firstname=john,lastname=doe,age=30"
     * @param callback  Called when the task is fully processed. The parameter is the requested nodes, empty array otherwise.
     * @param world     The world id in which the search must be performed.
     * @param time      The timepoint at which the search must be performed.
     */
    <A extends Node> void findAt(String indexName, long world, long time, String query, Callback<A[]> callback);

    /**
     * Retrieves all nodes in a particular index at the current node world and the current node time
     *
     * @param indexName The name of the index
     * @param callback  Called whe the collection is complete. Gives the list of contained nodes in parameter.
     */
    <A extends Node> void all(String indexName, Callback<A[]> callback);

    /**
     * Retrieves all nodes in a particular index and in a particular world and time
     *
     * @param indexName The name of the index
     * @param callback  Called whe the collection is complete. Gives the list of contained nodes in parameter.
     * @param world     The world id in which the search must be performed.
     * @param time      The timepoint at which the search must be performed.
     */
    <A extends Node> void allAt(String indexName, long world, long time, Callback<A[]> callback);

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
     * Retrieves all timePoints fromVar the timeLine of this node when alterations occurred.<br>
     * This method also jumps over the world hierarchy to collect all available timepoints.<br>
     * To unbound the search, please use {@link Constants#BEGINNING_OF_TIME} and {@link Constants#END_OF_TIME} as bounds.
     *
     * @param beginningOfSearch (inclusive) earliest bound for the search.
     * @param endOfSearch       (inclusive) latest bound for the search.
     * @param callback          Called when the search is finished. Provides an array containing all the timepoints required.
     */
    void timepoints(long beginningOfSearch, long endOfSearch, Callback<long[]> callback);

    /**
     * Informs mwDB memory manager that this node object can be freed fromVar the memory.<br>
     * <b>Warning: this MUST be the last method called on this node.</b><br>
     * To work selectWith the node afterwards, a new lookup is mandatory.
     */
    void free();

    /**
     * Return the graph that have created this node.
     *
     * @return the graph this node belongs to
     */
    Graph graph();

    /**
     * Jump over the time for this object. This method is equivalent to a call to lookup selectWith the same ID than the current Node.
     *
     * @param targetTime target time selectWhere this node hasField to be resolved.
     * @param callback   Called whe the jump is complete. Gives the new timed node in parameter.
     * @param <A>        Generic parameter that define the type of the result, should be a sub-type of Node
     */
    <A extends Node> void jump(long targetTime, Callback<A> callback);

}
