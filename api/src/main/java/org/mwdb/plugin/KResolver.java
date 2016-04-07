package org.mwdb.plugin;

import org.mwdb.KCallback;
import org.mwdb.KGraph;
import org.mwdb.KNode;

public interface KResolver {

    /**
     * Initializes the resolver.
     */

    /**
     * Initializes the resolver with the graph passed as parameter
     *
     * @param graph graph this resolver belongs to
     */
    void init(KGraph graph);

    /**
     * Initializes backend structures for the newly created node passed as parameter
     *
     * @param node     The node to initialize.
     * @param typeCode The coded type param to initialize
     */
    void initNode(KNode node, long typeCode);

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
    void freeNode(KNode node);

    /**
     * Creates a lookup task to retrieve a particular node based on world/time/node_id
     *
     * @param world    The world identifier
     * @param time     The timepoint.
     * @param id       The id of the node to retrieve.
     * @param callback Called when the node is retrieved.
     * @return The created lookup task to be given to the scheduler.
     */
    <A extends KNode> KCallback lookupTask(long world, long time, long id, KCallback<A> callback);

    /**
     * Creates and schedules a lookup task.
     *
     * @param world    The world identifier
     * @param time     The timepoint.
     * @param id       The id of the node to retrieve.
     * @param callback Called when the node is retrieved.
     */
    <A extends KNode> void lookup(long world, long time, long id, KCallback<A> callback);

    /**
     * Resolves the state of a node, to access attributes, relations, and indexes.
     * In case dephasing is allowed the latest state available is returned.
     * In case dephasing is not allowed (false), the state is phased, i.e.: cloned at the timePoint of the node.
     *
     * @param node           The node for which the state mush be collected.
     * @param allowDephasing Specifies if the requested state can be dephased. If not, it will be cloned at the timepoint of the node.
     * @return The resolved state of the node.
     */
    KNodeState resolveState(KNode node, boolean allowDephasing);

    /**
     * Resolves the timePoints of a node.
     *
     * @param node              The node for which timepoints are requested.
     * @param beginningOfSearch The earliest timePoint of the search (included).
     * @param endOfSearch       The latest timePoint of the search (included).
     * @param callback          Called when finished, with the list of timepoints included in the bounds for this node.
     */
    void resolveTimepoints(KNode node, long beginningOfSearch, long endOfSearch, KCallback<long[]> callback);

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

    /**
     * Node state
     */
    interface KNodeState {

        /**
         * Access to the world whom this state is attached to
         *
         * @return current resolved world
         */
        long world();

        /**
         * Access to the time whom this state is attached to
         *
         * @return current resolved time
         */
        long time();

        /**
         * Set the named state element
         *
         * @param index    unique key of element
         * @param elemType type of the element (based on KType definition)
         * @param elem     element to be set
         */
        void set(long index, byte elemType, Object elem);

        /**
         * Get the named state element
         *
         * @param index unique key of element
         * @return stored element
         */
        Object get(long index);

        /**
         * Atomically get or create an element according to the elemType parameter.
         * This method is particularly handy for map manipulation that have to be initialize by the node state before any usage.
         *
         * @param index    unique key of element
         * @param elemType type of the element (according to KType definition)
         * @return new or previously stored element
         */
        Object getOrCreate(long index, byte elemType);

        /**
         * Get the type of the stored element, -1 if not found
         *
         * @param index unique key of element
         * @return type currently stored, encoded as a int according the KType defintion
         */
        byte getType(long index);

    }

}
