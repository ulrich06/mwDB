package org.mwdb.chunk;

public interface KStringLongMap {

    long getValue(String key);

    String getKey(long index);

    /**
     * NULL_LONG as value means replace atomically by the current
     */
    void put(String key, long value);

    void each(KStringLongMapCallBack callback);

    int size();

    void remove(String key);

}
