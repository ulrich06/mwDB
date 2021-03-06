package org.mwg.core.chunk;

public interface LongTree {

    void insert(long key);

    void unsafe_insert(long key);

    long previousOrEqual(long key);

    void clearAt(long max);

    void range(long startKey, long endKey, long maxElements, TreeWalker walker);

    long magic();

    long size();

}
