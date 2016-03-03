package org.mwdb.chunk;

public interface KStringLongMap {

    boolean contains(String key);

    long getValue(String key);

    String getKey(long index);

    void put(String key, long value);

    void each(KStringLongMapCallBack callback);

    int size();

    void clear();

    void remove(String key);

}
