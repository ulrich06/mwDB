package org.mwg.core.chunk;

public interface KLongTree {

    void insert(long key);

    long previousOrEqual(long key);

    void clearAt(long max);

    void range(long startKey, long endKey, long maxElements, KTreeWalker walker);

    long magic();

    long size();

}
