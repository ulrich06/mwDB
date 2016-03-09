package org.mwdb.chunk;

public interface KStringLongMap {
    
    long getValue(String key);


    String getKey(long index);

    /**
     * Add the tuple key/value to the map.
     * In case the value is null, the value will be atomically replaced by the current size of the map and associated to the key
     *
     * @param key   to insert key
     * @param value to insert value
     */
    void put(String key, long value);

    void remove(String key);

    /**
     * Iterate over all Key/value tuple of the cam
     *
     * @param callback closure that will be called for each K/V tuple
     */
    void each(KStringLongMapCallBack callback);

    /**
     * Size of the map
     *
     * @return the size of the map
     */
    int size();

}
