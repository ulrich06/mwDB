package org.mwdb;

public interface KNode {

    /**
     * KNode identification
     */
    long world();

    long time();

    long id();

    /**
     * Attributes Management
     */
    Object att(String attributeName);

    void attSet(String attributeName, int attributeType, Object attributeValue);

    void attRemove(String attributeName, Object attributeValue);

    /**
     * Relationships Management
     */
    void ref(String relationName, KCallback<KNode[]> callback);

    void refValues(String relationName, long[] ids);

    void refAdd(String relationName, KNode relatedNode);

    void refRemove(String relationName, KNode relatedNode);

    /**
     * Synchronous Wrapper
     */
    KNode[] refSync(String relationName);

    /**
     * Memory Management
     */
    void free();

    /**
     * Utility methods
     */
    long timeDephasing();

    void undephase();

    void timepoints(KCallback<long[]> callback);


    /**
     * Time related naviguation
     */
    /*

    void allTimes(KCallback<long[]> cb);

    void timesBefore(long endOfSearch, KCallback<long[]> cb);

    void timesAfter(long beginningOfSearch, KCallback<long[]> cb);

    void timesBetween(long beginningOfSearch, long endOfSearch, KCallback<long[]> cb);
*/

    /**
     * Bulk KNode management
     */
    String toJSON();

    /*
    boolean equals(Object other);


    KMetaRelation[] referencesWith(KNode o);

    void invokeOperation(KMetaOperation operation, Object[] params, KOperationStrategy strategy, KCallback cb);

    void invokeOperationByName(String operationName, Object[] params, KOperationStrategy strategy, KCallback cb);

    KDataManager manager();

    KMeta[] compare(KNode target);
    */

}
