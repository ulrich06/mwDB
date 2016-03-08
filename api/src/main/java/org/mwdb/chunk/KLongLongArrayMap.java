package org.mwdb.chunk;

public interface KLongLongArrayMap {

    long[] get(long key);

    void put(long key, long value);

    void remove(long key, long value);

    void each(KLongLongArrayMapCallBack callback);

    int size();

}
