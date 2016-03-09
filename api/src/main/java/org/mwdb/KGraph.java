package org.mwdb;

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
     * Many world management
     */
    long diverge(long world);

    /**
     * Life cycle management
     */
    void save(KCallback callback);

    void connect(KCallback callback);

    void disconnect(KCallback callback);

    /**
     * Global indexes management
     */
    void index(String indexName, KNode toIndexNode, String[] keyAttributes, KCallback callback);

    void find(long world, long time, String indexName, String query, KCallback<KNode> callback);

    void all(long world, long time, String indexName, KCallback<KNode[]> callback);

    /**
     * Utilities
     */
    KDeferCounter counter(int expectedCountCalls);

}
