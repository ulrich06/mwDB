package org.mwdb.chunk;

public interface KLongLongMap {

    long get(long key);

    void put(long key, long value);

    void remove(long key);

    void each(KLongLongMapCallBack callback);

    int size();

    // void clear();

    // long magic();

    /**
     * extra methods
     */
    /*
    int extra();

    boolean tokenCompareAndSwap(int previous, int next);
    */

}
