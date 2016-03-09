package org.mwdb.chunk;

public interface KLongLongMap {

    /**
     * Retrieve the value inserted with the param key
     *
     * @param key key that have to be retrieve
     * @return associated value, Constants.NULL.LONG in case of not found.
     */
    long get(long key);

    /**
     * Add the tuple key/value to the map.
     * In case the value is equals to Constants.NULL_LONG, the value will be atomically replaced by the current size of the map
     *
     * @param key
     * @param value
     */
    void put(long key, long value);

    /**
     * Remove the key passed as parameter from the map
     *
     * @param key key that have to be removed
     */
    void remove(long key);

    /**
     * Iterate over all Key/value tuples of the cam
     *
     * @param callback closure that will be called for each K/V tuple
     */
    void each(KLongLongMapCallBack callback);

    /**
     * Size of the map
     *
     * @return the size of the map
     */
    int size();

}
