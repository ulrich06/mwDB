package org.mwg.plugin;

import org.mwg.*;

/**
 * Resolver plugin, able to change the semantic of Many World Graph
 */
public interface Resolver {

    /**
     * Initializes the resolver selectWith the graph passed as parameter
     *
     * @param graph graph this resolver belongs to
     */
    void init(Graph graph);

    /**
     * Initializes backend structures for the newly created node passed as parameter
     *
     * @param node     The node to initialize.
     * @param typeCode The coded type param to initialize
     */
    void initNode(Node node, long typeCode);

    /**
     * Initializes a newly created world, and sets the parent relationship.
     *
     * @param parentWorld The parent world
     * @param childWorld  The new world to initialize.
     */
    void initWorld(long parentWorld, long childWorld);

    /**
     * Frees a node structure so it might be recycled.
     *
     * @param node The node to free.
     */
    void freeNode(Node node);

    /**
     * Creates a lookup job to retrieve a particular node based on world/time/node_id
     *
     * @param world    The world identifier
     * @param time     The timepoint.
     * @param id       The id of the node to retrieve.
     * @param callback Called when the node is retrieved.
     * @return The created lookup job to be given to the scheduler.
     */
    <A extends Node> Job lookupJob(long world, long time, long id, Callback<A> callback);

    /**
     * Creates and schedules a lookup task.
     *
     * @param world    The world identifier
     * @param time     The timepoint.
     * @param id       The id of the node to retrieve.
     * @param callback Called when the node is retrieved.
     */
    <A extends Node> void lookup(long world, long time, long id, Callback<A> callback);

    /**
     * Resolves the state of a node, to access attributes, relations, and indexes.
     * In case dephasing is allowed the latest state available is returned.
     * In case dephasing is not allowed (false), the state is phased, i.e.: cloned at the timePoint of the node.
     *
     * @param node           The node for which the state must be collected.
     * @param allowDephasing Specifies if the requested state can be dephased. If not, it will be cloned at the timepoint of the node.
     * @return The resolved state of the node.
     */
    NodeState resolveState(Node node, boolean allowDephasing);

    /**
     * @param node  The node for which the state must be collected.
     * @param world The world for which the new state must be created.
     * @param time  The time for which the new state must be created.
     * @return The newly empoty created state of the node.
     */
    NodeState newState(Node node, long world, long time);

    /**
     * Resolves the timePoints of a node.
     *
     * @param node              The node for which timepoints are requested.
     * @param beginningOfSearch The earliest timePoint of the search (included).
     * @param endOfSearch       The latest timePoint of the search (included).
     * @param callback          Called when finished, selectWith the list of timepoints included in the bounds for this node.
     */
    void resolveTimepoints(Node node, long beginningOfSearch, long endOfSearch, Callback<long[]> callback);

    /**
     * Maps a String to a unique long. Can be reversed using {@link #longKeyToString(long)}.
     *
     * @param name The string value to be mapped.
     * @return The unique long identifier for the string.
     */
    long stringToLongKey(String name);

    /**
     * Returns the String associated to a long key.
     *
     * @param key The long key.
     * @return The string value associated to the long key.
     */
    String longKeyToString(long key);

}
