package org.mwdb;

import org.mwdb.plugin.KStorage;

public interface KGraph {

    /**
     * Node management
     */
    KNode createNode(long world, long time);

    /**
     * Lookup management
     */
    void lookup(long world, long time, long id, KCallback<KNode> callback);

    void lookupAllTimes(long world, long[] times, long id, KCallback<KNode[]> callback);

    /**
     * Life cycle management
     */
    void save(KCallback callback);

    void connect(KCallback callback);

    void disconnect(KCallback callback);

    /**
     * Utilities
     */
    KDeferCounter counter(int expectedCountCalls);

}
