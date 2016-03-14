package org.mwdb.chunk;

public interface KLongTree {

    void insert(long key);

    long previousOrEqual(long key);

    void range(long startKey, long endKey, KTreeWalker walker);

    long magic();

    long size();

}
