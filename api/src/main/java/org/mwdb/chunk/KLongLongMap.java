package org.mwdb.chunk;

public interface KLongLongMap {

    long get(long key);

    /**
     * NULL_LONG as value means replace atomically by the current
     */
    void put(long key, long value);

    void remove(long key);

    void each(KLongLongMapCallBack callback);

    int size();

}
