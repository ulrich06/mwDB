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
     * Mark node used structure to unused, potentially to be recycled
     */
    void freeNode(KNode node);

    /**
     * Create a task to lookup a particular node
     */
    KCallback lookupTask(long world, long time, long id, KCallback<KNode> callback);

    /**
     * Create and scheduler a task to lookup a particular node
     */
    void lookup(long world, long time, long id, KCallback<KNode> callback);

    /**
     * Resolve the node state according to the resolution function, the second parameter define if this state should be aligned or not with the node
     */
    KNodeState resolveState(KNode node, boolean allowDephasing);

    /**
     * Internal access to the dictionary
     */
    long key(String name);

    String value(long key);

}
