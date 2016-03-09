package org.mwdb.chunk;

public interface KLongLongArrayMap {

    long[] get(long key);

    /**
     * Add the tuple key/value to the map.
     *
     * @param key   to insert key
     * @param value to insert value
     */
    void put(long key, long value);

    void remove(long key, long value);

    /**
     * Iterate over all Key/value tuple of the cam
     *
     * @param callback closure that will be called for each K/V tuple
     */
    void each(KLongLongArrayMapCallBack callback);

    /**
     * Size of the map
     *
     * @return the size of the map
     */
    int size();

}
