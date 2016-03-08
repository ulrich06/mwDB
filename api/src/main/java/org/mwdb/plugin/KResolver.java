package org.mwdb.plugin;

import org.mwdb.KCallback;
import org.mwdb.KNode;

public interface KResolver {

    /**
     * Init the resolver (to enforce optimizations)
     */
    void init();

    /**
     * Init structure for a newly created node
     */
    void initNode(KNode node);

    /**
     * Init world according to the parent
     */
    void initWorld(long parentWorld, long childWorld);

    /**
     * Mark node used structure to unused, potentially to be recycled
     */
    void freeNode(KNode node);

    /**
     * Create a task to lookup a particular node
     */
    KCallback lookupTask(long world, long time, long id, KCallback<KNode> callback);

    /**
     * Create and schedule a task to lookup a particular node
     */
    void lookup(long world, long time, long id, KCallback<KNode> callback);

    /**
     * Resolve the node state according to the resolution function, the second parameter define if this state should be aligned or not with the node
     */
    KNodeState resolveState(KNode node, boolean allowDephasing);

    /**
     * Resolve timepoints in a particular range
     */
    void resolveTimepoints(KNode origin, long beginningOfSearch, long endOfSearch, KCallback<long[]> callback);

    /**
     * Internal access to the dictionary
     */
    long key(String name);

    String value(long key);

    /**
     * Node state return by the resolver
     */
    interface KNodeState {

        void set(long index, int elemType, Object elem);

        Object get(long index);

        Object getOrCreate(long index, int elemType);

    }

}
