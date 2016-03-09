package org.mwdb.plugin;

import org.mwdb.KCallback;
import org.mwdb.KNode;

public interface KResolver {

    /**
     * Init the resolver (to enforce optimizations)
     */
    void init();

    /**
     * Init background structures for the newly created node passed as parameter
     *
     * @param node newly created node
     */
    void initNode(KNode node);

    /**
     * Init the newly created world, and register the parent relationship.
     *
     * @param parentWorld world with shared past data
     * @param childWorld  newly created world id
     */
    void initWorld(long parentWorld, long childWorld);

    /**
     * Mark node used structure to unused, potentially to be recycled
     *
     * @param node node to be recycled
     */
    void freeNode(KNode node);

    /**
     * Create a lookup task, to retrieve a particular node based on world/time/node_id
     *
     * @param world    current world id
     * @param time     current timePoint
     * @param id       node id to resolve
     * @param callback result closure
     * @return task that have to be scheduled later
     */
    KCallback lookupTask(long world, long time, long id, KCallback<KNode> callback);

    /**
     * Create and schedule lookup task, to retrieve a particular node based on world/time/node_id
     *
     * @param world    current world id
     * @param time     current timePoint
     * @param id       node id to resolve
     * @param callback result closure
     */
    void lookup(long world, long time, long id, KCallback<KNode> callback);

    /**
     * Resolve the state of a node (where are stored attributes, relations, indexes...)
     * In case dephasing is allowed the previous or state is taken.
     * In case dephasing is false, the state has to be in phased and will be cloned to the same timePoint than the current node.
     *
     * @param node           origin node attached to the state
     * @param allowDephasing boolean parameter to allows or not dePhasing for the resolved state.
     * @return
     */
    KNodeState resolveState(KNode node, boolean allowDephasing);

    /**
     * Resolve timePoints for a particular node
     *
     * @param origin            current node from where the resolution has to start
     * @param beginningOfSearch lower timePoint bound of the search
     * @param endOfSearch       upper timePoint bound of the search
     * @param callback          result closure
     */
    void resolveTimepoints(KNode origin, long beginningOfSearch, long endOfSearch, KCallback<long[]> callback);

    /**
     * Encode a string a unique key or return the previously existing one if already encoded.
     *
     * @param name value to be encoded
     * @return unique key
     */
    long key(String name);

    /**
     * Convert a dictionary key to the previously encoded string
     *
     * @param key dictionary key
     * @return encoded value
     */
    String value(long key);

    /**
     * Node state return by the resolver
     */
    interface KNodeState {

        /**
         * Set the named state element
         *
         * @param index    unique key of element
         * @param elemType type of the element (based on KType definition)
         * @param elem     element to be set
         */
        void set(long index, int elemType, Object elem);

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
        Object getOrCreate(long index, int elemType);

    }

}
